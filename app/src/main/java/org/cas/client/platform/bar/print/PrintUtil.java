package org.cas.client.platform.bar.print;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;

import com.just.print.R;
import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.CASUtility;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.casutil.L;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrintUtil {

	public static final String CUT = "cut";
	public static final String REF_TO = "#ref to:";
	public static final String DATE_REF = "*date_ref:";
	public static final String OLD_SUBTOTAL = "*Old Subtotal:";
	public static final String OLD_GST = "*Old GST:";
	public static final String OLD_QST = "*Old QST:";
	public static final String OLD_TOTAL = "*Old Total:";

	private static ExecutorService executorService = Executors.newSingleThreadExecutor();
	public static List<String> errorPrinterList = new ArrayList<String>();
	
	static PrintManager defaultService;
	
	static String getTitle(Character key) {
		switch (key) {
		case 'I':
			return "DineIn";
		case '0':
			return "ToGo";
		case 'G':
			return "ToGo";
		case 'P':
			return "ToGo";
		case 'U':
			return "Uber";
		case 'H':
			return "Hicity";
		case 'X':
			return "XHC";
		case 'D':
			return "Deliver";
		case 'F':
			return "FanTuan";
		}
		
		return String.valueOf(key);
	}

	static int getNumberStrLength(int cashQt, String cashTotal) {
		return String.valueOf(cashQt).length() + cashTotal.length();
	}

	static void checkPrinter() {
		//check printer.
	    if(defaultService == null) {
		    defaultService = (PrintManager) BarFrame.instance.getSystemService(Context.PRINT_SERVICE);
		    if(defaultService == null) {
			    defaultService = (PrintManager) BarFrame.instance.getSystemService(Context.PRINT_SERVICE);

				if (defaultService == null) {
					JOptionPane.showMessageDialog("Please install printer first.");
					L.e("PrintUtil", "Please install printer first.", null);
				}
		    }
	    }
	}

	public static Path getSerialCommandFilePath(String fileName, byte[] command) {
		String filePath = CASUtility.getPIMDirPath().concat(fileName);
		Path path = Paths.get(filePath);
		File file = new File(filePath);
		
		//check if it's exist.
		if(!file.exists()) {
			//creata the file
			try {
				byte[] c = new byte[command.length];
				System.arraycopy(command, 0, c, 0, command.length);
				Files.write(path, c);
			}catch(Exception e) {
				ErrorUtil.write(e);
			}
		}
	
		return path;
	}

	public static String combinedPrint(String part1, Bitmap qrcodeImage, String part2) {
		try {
			// 处理文本内容
			List<String> lines1 = part1 == null ? new ArrayList<String>() : Arrays.asList(part1.split("\n"));
			List<String> lines2 = part2 == null ? new ArrayList<String>() : Arrays.asList(part2.split("\n"));

			Paint paint = new Paint();
			paint.setTypeface(Typeface.create("宋体", Typeface.NORMAL));
			paint.setTextSize(12);

			// 计算文本的宽度和高度
			int maxWidth = (int) paint.measureText("-");
			int totalHeight = (int) (paint.getFontMetrics().bottom - paint.getFontMetrics().top) * (lines1.size() + lines2.size() + 2);
			if (qrcodeImage != null) {
				totalHeight += qrcodeImage.getHeight();
			}

			Bitmap combinedImage = Bitmap.createBitmap(maxWidth, totalHeight, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(combinedImage);
			canvas.drawColor(Color.WHITE); // 设置背景颜色为白色

			paint.setColor(Color.BLACK); // 设置绘制颜色为黑色

			// 绘制第一段文本
			int y = drawFirstPart(lines1, canvas, paint, maxWidth);

			if (qrcodeImage != null) {
				// 绘制二维码
				canvas.drawBitmap(qrcodeImage, (maxWidth - qrcodeImage.getWidth()) / 2, y, null);
				y += qrcodeImage.getHeight();

				// 绘制第二段文本
				for (String line : lines2) {
					line = line.trim();
					int textWidth = (int) paint.measureText(line); // 获取文本宽度
					int startX = (maxWidth - textWidth) / 2; // 计算文本的起始X坐标，使其居中

					canvas.drawText(line, startX, y, paint);
					y += (int) (paint.getFontMetrics().bottom - paint.getFontMetrics().top);
				}
			}

			// 发送到打印机（需要实现 sendToPrinter 方法）
			sendToPrinter(totalHeight, combinedImage, BarFrame.instance);

			// 保存为PDF（需要实现 writeToPdf 方法）
			return writeToPdf(combinedImage, BarFrame.instance);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static int drawFirstPart(List<String> lines, Canvas canvas, Paint paint, int maxWidth) {
		int y = (int) (-paint.getFontMetrics().top); // 初始 y 坐标
		for (String line : lines) {
			line = line.trim();
			int textWidth = (int) paint.measureText(line); // 获取文本宽度
			int startX = (maxWidth - textWidth) / 2; // 计算文本的起始X坐标，使其居中

			canvas.drawText(line, startX, y, paint);
			y += (int) (paint.getFontMetrics().bottom - paint.getFontMetrics().top);
		}
		return y;
	}

	private static void sendToPrinter(int totalHeight, Bitmap combinedImage, Context context) {
		PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
		String jobName = context.getString(R.string.app_name) + " Document";
		printManager.print(jobName, new BitmapPrintDocumentAdapter(context, combinedImage), null);
	}

	private static String writeToPdf(Bitmap combinedImage, Context context) {
		PdfDocument pdfDocument = new PdfDocument();
		PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(combinedImage.getWidth(), combinedImage.getHeight(), 1).create();
		PdfDocument.Page page = pdfDocument.startPage(pageInfo);
		Canvas canvas = page.getCanvas();
		canvas.drawBitmap(combinedImage, 0, 0, null);
		pdfDocument.finishPage(page);

		try {
			File file = new File(context.getExternalFilesDir(null), "PrintDocument.pdf");
			FileOutputStream outputStream = new FileOutputStream(file);
			pdfDocument.writeTo(outputStream);
			pdfDocument.close();
			outputStream.close();
			return file.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	private static void sendToPrinter(Bitmap combinedImage, Context context) {
		// 创建打印管理器
		PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);

		// 创建一个打印适配器
		PrintDocumentAdapter printAdapter = new PrintDocumentAdapter() {
			@Override
			public void onStart() {
				super.onStart();
			}

			@Override
			public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
				if (cancellationSignal.isCanceled()) {
					callback.onLayoutCancelled();
					return;
				}

				PrintDocumentInfo.Builder builder = new PrintDocumentInfo.Builder("combined_print");
				builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
						.setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
						.build();

				callback.onLayoutFinished(builder.build(), true);
			}

			@Override
			public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
				OutputStream out = null;
				try {
					out = new FileOutputStream(destination.getFileDescriptor());

					// 将Bitmap写入PDF
					PdfDocument pdfDocument = new PdfDocument();
					PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(combinedImage.getWidth(), combinedImage.getHeight(), 1).create();
					PdfDocument.Page page = pdfDocument.startPage(pageInfo);

					Canvas canvas = page.getCanvas();
					canvas.drawBitmap(combinedImage, 0, 0, null);

					pdfDocument.finishPage(page);

					pdfDocument.writeTo(out);
					pdfDocument.close();

					callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
				} catch (IOException e) {
					e.printStackTrace();
					callback.onWriteFailed(e.toString());
				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};

		// 启动打印对话框
		printManager.print("combined_print_job", printAdapter, new PrintAttributes.Builder().build());
	}


//	// 将BufferedImage保存为PDF
//	private static String writeToPdf(BufferedImage combinedImage) throws IOException, FileNotFoundException {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		ImageIO.write(combinedImage, "PNG", baos);
//		byte[] imageInByte = baos.toByteArray();
//		baos.close();
//
//		ImageData imageData = ImageDataFactory.create(imageInByte);
//		Image pdfImg = new Image(imageData);
//
//		// Specify the path to save the PDF
//		String key = BarUtil.sdf.format(new Date());
//		//write text part content to a file.
//		String dest = CASUtility.getPIMDirPath().concat(key + "transaction.pdf");
//		PdfWriter writer = new PdfWriter(dest);		//~~~~this step generate an empty file on hard disk!~~~
//		PdfDocument pdfDoc = new PdfDocument(writer);
//		Document document = new Document(pdfDoc);
//
//		// Add image to PDF
//		document.add(pdfImg);
//
//		document.close();	//all the content in document is now write to the file on hard disk~~~~
//		return key;
//	}
//
//	private static int drawFisrtPart(List<String> lines1, Graphics2D g, int maxWidth) {
//	    int y = 20; //@NOTE can not print from 0, because seems the first line was printed out of the area.(can not be seen)
//	    if(lines1.size() == 0) {
//	        return y;
//	    }
//	    boolean startToLeftAline = false;
//	    for (int i = 0; i < lines1.size() - 1; i++) {
//	        String line = lines1.get(i).trim();
//	        int startP = 0;
//	        if(!startToLeftAline && line.equals(BarUtil.getSeperatorLine(0))) {
//	            startToLeftAline = true;
//	            line += line.substring(0, 30);
//	        }
//	        if(!startToLeftAline) {
//	            startP = (maxWidth - g.getFontMetrics().stringWidth(line)) / 2;
//	        }
//
//	        // Check for consecutive spaces when startP is zero
//	        if (startP == 0) {
//	            int lastSpaceIndex = line.lastIndexOf("  ");
//	            if (lastSpaceIndex != -1) {
//	                String leftPart = line.substring(0, lastSpaceIndex);
//	                String rightPart = line.substring(lastSpaceIndex).trim();
//	                g.drawString(leftPart, 0, y);
//	                g.drawString(rightPart, maxWidth - g.getFontMetrics().stringWidth(rightPart), y);
//	                y += g.getFontMetrics().getHeight();
//	                continue;
//	            }
//	        }
//
//	        g.drawString(line, startP, y);
//	        y += g.getFontMetrics().getHeight();
//	    }
//	    String lastLine = lines1.get(lines1.size() - 1).trim();
//	    g.drawString(lastLine, (maxWidth - g.getFontMetrics().stringWidth(lastLine)) / 2, y);
//	    return y;
//	}

	public static boolean printThroughOSdriver(Path path1, File qrcodeImg, Path path2, boolean deleteCommandFile) {
		//if qrcodeImg is null, then treated as calling the two parameters method.
		if (qrcodeImg == null) {
			boolean status = printThroughOSdriver(PrintUtil.getMevCommandFilePath(path1, Command.GS_V_m_n), false);
			flushout();
			return status;
		}
		//qrcodeImg is not null, then also call into the 2 param method to print out the text part.
		if(!printThroughOSdriver(path1, deleteCommandFile)) {
			return false;
		}

		if(path2 != null && !printThroughOSdriver(path2, deleteCommandFile)) {
			return false;
		}

//		//then print out the qrcode part.
//		DocPrintJob job = defaultService.createPrintJob();
//		PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
//
//		pras.add(OrientationRequested.PORTRAIT);
//		pras.add(PrintQuality.HIGH);
//		pras.add(new Copies(1));
//		pras.add(MediaSizeName.ISO_A7);
//
//		DocAttributeSet das = new HashDocAttributeSet();
//		das.add(new MediaPrintableArea(0.3f, 0, 2, 2, MediaPrintableArea.INCH));
//		try {
//			FileInputStream fin = new FileInputStream(qrcodeImg);
//			Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.PNG, das);
//
//			job.print(doc, pras);
//			fin.close();
//		} catch (PrintException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}


		flushout();

		return true;
	}

	private static void flushout() {
		byte[] resetCommand = new byte[]{(byte) 0x0C};  // FF (Form Feed)

//        InputStream is = new ByteArrayInputStream(resetCommand);
//
//        // 获取默认打印服务
//        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
//
//        // 创建打印任务
//        DocPrintJob printJob = service.createPrintJob();
//        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
//        Doc doc = new SimpleDoc(is, flavor, null);
//
//        // 发送打印任务
//        try {
//			printJob.print(doc, null);
//		} catch (PrintException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public static boolean printThroughOSdriver(Path path, boolean deleteCommandFile) {
//		try {
//			DocPrintJob job = defaultService.createPrintJob();
//	    	FileInputStream stream = new FileInputStream(path.toFile());
//
//	    	PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
//			DocAttributeSet das = new HashDocAttributeSet();
//
//			job.print(new SimpleDoc(stream,  DocFlavor.INPUT_STREAM.AUTOSENSE, das), pras);
//			if(deleteCommandFile) {
//				Files.deleteIfExists(path);
//			}
//			return true;
//		} catch (PrintException e) {
//			L.e("PrintService", ": Print through OS", e);
//		} catch(IOException ioe) {
//			L.e("PrintService", ": Print through OS", ioe);
//		}
		return false;
	}

	public static Path getMevCommandFilePath(String fileName, byte[] command) {
		String filePath = CASUtility.getPIMDirPath().concat(fileName);
		Path path = Paths.get(filePath);
	
		return getMevCommandFilePath(path, command);
	}
	
	public static Path getMevCommandFilePath(Path path, byte[] command) {
		if(command == null) {
			return path;
		}
		//check if it's exist.
		if(!path.toFile().exists()) { //creata the file
			try {
				Files.write(path, command);
			}catch(Exception e) {
				L.e("PrintUtil ", "exception when getMevCommandFilePath.", e);
			}
		}else {
			appendToFile(path.toFile(), command);
		}
	
		return path;
	}
	
    public static void appendToFile(File file, byte[] data) {
        try (FileOutputStream out = new FileOutputStream(file, true)) { // 注意这里的第二个参数设置为true表示启用追加模式
            out.write(data);
        } catch (IOException e) {
        	L.e("PrintUtil", "Exception when appending command into the file", e);
            e.printStackTrace();
        }
    }
    
	// Send QRCode image to the printer
	public static void sendQRCodeToPrinter(OutputStream outputStream, Bitmap qrCodeImage) throws IOException {
		// Convert Bitmap to byte array
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		qrCodeImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
		byte[] imageBytes = byteArrayOutputStream.toByteArray();

		// ESC/POS command to print an image (may vary based on printer model)
		outputStream.write(Command.GS_k_m_v_r_nL_nH);

		try {
			// 选择二维码模块大小
			byte[] selectQRSize = new byte[]{0x1D, 'z', 0x02};  // 假设n为2，具体值根据手册
			outputStream.write(selectQRSize);

			// 选择错误纠正等级
			byte[] errorCorrection = new byte[]{0x1D, 'y', 0x02};  // 假设n为2，具体值根据手册
			outputStream.write(errorCorrection);

			// 存储QRCode数据
			String qrData = "https://www.example.com"; // QRCode内容
			byte[] storeQRData = new byte[3 + qrData.length()];
			storeQRData[0] = 0x1D;
			storeQRData[1] = 'd';
			storeQRData[2] = (byte) qrData.length(); // 假设QRCode内容不超过255字节
			System.arraycopy(qrData.getBytes(), 0, storeQRData, 3, qrData.length());
			outputStream.write(storeQRData);

			// 打印QRCode数据
			byte[] printQRCode = new byte[]{0x1D, 'm'};
			outputStream.write(printQRCode);

			outputStream.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Send the image data to the printer
		outputStream.write(imageBytes);
		outputStream.flush();
	}

	//
//    private static byte[] generateQRCodeCommand(String data) {
//        // This is a simplified example, and the actual command might vary 
//        // based on the printer's ESC/POS command documentation.
//        ByteArrayOutputStream command = new ByteArrayOutputStream();
//
//        // QR code size, error correction, and other parameters might be set here
//
//        // Command to print QR code with data
//        command.write(0x1D);  // GS (Group Separator)
//        command.write('(');   // (
//        command.write('k');  // k
//        // ... more parameters, including data length and the actual data bytes
//
//        return command.toByteArray();
//    }
//
	public static CompletableFuture<String> doWebSocketPrint(String ip, String mainContent, Bitmap image, String responseFromWebMev) {
		if (ip == null || ip.length() == 0)
			return CompletableFuture.completedFuture(null);

		String[] ipAry = ip.split("\\.");
		if (ipAry.length != 4) {
			L.e("Invalidate Ip found in doWebSocketPrint, the ip is:", ip, null);
			return CompletableFuture.completedFuture(null);
		}

		return CompletableFuture.supplyAsync(() -> {
			Socket socket = null;
			OutputStream outputStream = null;
			try {
				InetAddress inet = InetAddress.getByAddress(new byte[]{
						Short.valueOf(ipAry[0]).byteValue(), Short.valueOf(ipAry[1]).byteValue(),
						Short.valueOf(ipAry[2]).byteValue(), Short.valueOf(ipAry[3]).byteValue()});
				if (!inet.isReachable(BarOption.getPrinterMinWaiTime())) {
					L.e("printer slow. ip is: ", ip, null);
					return null;
				}

				socket = new Socket(ip, 9100);
				outputStream = socket.getOutputStream();
				if(image != null) {
					outputStream.write(getImageBytes_chatGPT(image));
				}
				// Write main content
				sendContentOutThroughStream(mainContent, outputStream, ip);

				// Example of sending QR code commands to the printer
				byte[] selectQRSize = new byte[]{0x1D, 'z', 0x02};  // Example size command
				outputStream.write(selectQRSize);

				byte[] errorCorrection = new byte[]{0x1D, 'y', 0x02};  // Example error correction command
				outputStream.write(errorCorrection);

				String qrData = "https://www.example.com"; // Example QR code content
				byte[] storeQRData = new byte[3 + qrData.length()];
				storeQRData[0] = 0x1D;
				storeQRData[1] = 'd';
				storeQRData[2] = (byte) qrData.length();
				System.arraycopy(qrData.getBytes(), 0, storeQRData, 3, qrData.length());
				outputStream.write(storeQRData);

				byte[] printQRCode = new byte[]{0x1D, 'm'};
				outputStream.write(printQRCode);

				outputStream.flush();

				// Example of sending an image to the printer
				sendQRCodeToPrinter(outputStream, image);

				outputStream.close();
				return "OK";
			} catch (Exception exp) {
				ErrorUtil.write(exp);
				L.e(ip, "Error when printing content to socket", exp);
				return null;
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (Exception exp) {
						L.e(ip, "Error when trying to close outputStream", exp);
					}
				}
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception exp) {
						L.e(ip, "Error when trying to close socket", exp);
					}
				}
			}
		}, executorService);
	}

	static CompletableFuture<String> doWebSocketPrint(final String ip, final List<String> sndMsg) {
		if (ip == null || ip.length() == 0)
			return CompletableFuture.completedFuture(null);

		String[] ipAry = ip.split("\\.");
		if (ipAry.length != 4) {
			L.e("Invalidate Ip found in doWebSocketPrint, the ip is:", ip, null);
			return CompletableFuture.completedFuture(null);
		}

		return CompletableFuture.supplyAsync(() -> {
			Socket socket = null;
			OutputStream outputStream = null;
			try {
				InetAddress inet = InetAddress.getByAddress(new byte[]{
						Short.valueOf(ipAry[0]).byteValue(), Short.valueOf(ipAry[1]).byteValue(),
						Short.valueOf(ipAry[2]).byteValue(), Short.valueOf(ipAry[3]).byteValue()});
				if (!inet.isReachable(BarOption.getPrinterMinWaiTime())) {
					L.e("printer slow. ip is: ", ip, null);
					return null;
				}

				socket = new Socket(ip, 9100);
				outputStream = socket.getOutputStream();
				for (String msg : sndMsg) {
					if (msg.contains(REF_TO) && msg.substring(msg.indexOf(REF_TO) + REF_TO.length()).equals(sndMsg.get(0).substring(1))) {
						continue;
					}
					sendContentOutThroughStream(msg, outputStream, ip);
				}
				outputStream.close();
				socket.close();
				return "OK";
			} catch (Exception exp) {
				ErrorUtil.write(exp);
				L.e(ip, "Error when printing content to socket", exp);
				return null;
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (Exception exp) {
						L.e(ip, "Error when trying to close outputStream", exp);
					}
				}
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception exp) {
						L.e(ip, "Error when trying to close socket", exp);
					}
				}
			}
		}, executorService);
	}
	
    public static byte[] POS_S_SetQRcode(String strCodedata, int nWidthX, int nErrorCorrectionLevel) {
    	
		if (nWidthX < 2 | nWidthX > 6 | nErrorCorrectionLevel < 1 | nErrorCorrectionLevel > 4)
			return new byte[0];
		
		byte[] bCodeData = null;
		try {
			bCodeData = strCodedata.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return new byte[0];
		}
		Command.GS_w[2] = (byte) nWidthX;
		Command.GS_k_m_v_r_nL_nH[3] = 0x00;//设置规格为6
		Command.GS_k_m_v_r_nL_nH[4] = (byte) nErrorCorrectionLevel;
		Command.GS_k_m_v_r_nL_nH[5] = (byte) (bCodeData.length & 0xff);
		Command.GS_k_m_v_r_nL_nH[6] = (byte) ((bCodeData.length & 0xff00) >> 8);
		
		byte[] data = byteArraysToBytes(new byte[][]{Command.GS_w, Command.GS_k_m_v_r_nL_nH, bCodeData});
		return data;
	
	}
    
    public static byte[] byteArraysToBytes(byte[][] data) {
        int length = 0;
        for (int i = 0; i < data.length; i++)
            length += data[i].length;
        byte[] send = new byte[length];
        int k = 0;
        for (int i = 0; i < data.length; i++)
            for (int j = 0; j < data[i].length; j++)
                send[k++] = data[i][j];
        return send;
    }
    

	
	//==============================================
	static String doSerialPrint(List<String> sndMsg) {
//        CommPortIdentifier commPortIdentifier;
//        SerialPort tSerialPort = null;
//        DataOutputStream outputStream = null;
//        try {
//            Enumeration tPorts = CommPortIdentifier.getPortIdentifiers();
//            if (tPorts == null || !tPorts.hasMoreElements()) {
//            	JOptionPane.showMessageDialog(BarFrame.instance, "no comm ports found! please check the printer connection.");
//                return null;
//            }
//
//            while (tPorts.hasMoreElements()) {
//                commPortIdentifier = (CommPortIdentifier) tPorts.nextElement();
//                if (commPortIdentifier.getPortType() != CommPortIdentifier.PORT_SERIAL)
//                    continue;
//
//                //if (!commPortIdentifier.isCurrentlyOwned()) {
//                    tSerialPort = (SerialPort)commPortIdentifier.open("PrintService", 10000);//并口用"ParallelBlackBox"
//                    outputStream = new DataOutputStream(tSerialPort.getOutputStream());
//                    for (String msg : sndMsg) {
//                    	if(msg.contains(REF_TO) && msg.substring(msg.indexOf(REF_TO) + REF_TO.length()).equals(sndMsg.get(0).substring(1))) {
//                    		continue;
//                    	}
//            			sendContentOutThroughStream(msg, outputStream, "serial");
//					}
////                    outputStream.write(27); // 打印机初始化：
////                    outputStream.write(64);
////
////                    char[] tTime = pDate.toCharArray(); // 输出日期时间 输出操作员工号
////                    for (int i = 0; i < tTime.length; i++)
////                        outputStream.write(tTime[i]);
////
////                    outputStream.write(13); // 回车
////                    outputStream.write(10); // 换行
////                    outputStream.write(10); // 进纸一行
////
////                    outputStream.write(28); // 设置为中文模式：
////                    outputStream.write(38);
////                    String tContent = ((String) CustOpts.custOps.getValue(PosDlgConst.PrintTitle)).concat("\n");
////                    for (int i = 0, len = getUsedRowCount(); i < len; i++) { // 遍历有效行。
////                        tContent = tContent.concat((String) tblContent.getValueAt(i, 1)).concat("\n"); // 再取出品名
////                        tContent = tContent.concat((String) tblContent.getValueAt(i, 3)).concat("   "); // 再取出单价
////                        tContent = tContent.concat((String) tblContent.getValueAt(i, 2)).concat("   "); // 再取出数量
////                        tContent = tContent.concat((String) tblContent.getValueAt(i, 4)).concat("\n"); // 再取出小计
////                    }
////                    for (int i = 0; i < 4; i++)
////                        // 换行
////                        tContent = tContent.concat("\n");
////
////                    tContent = tContent.concat(PosDlgConst.SumTotal);
////                    tContent = tContent.concat(tfdShoudReceive.getText());
////                    tContent = tContent.concat(PosDlgConst.Unit).concat("   ");// 总计
////
////                    tContent = tContent.concat(PosDlgConst.Receive);
////                    tContent = tContent.concat(tfdActuallyReve.getText());
////                    tContent = tContent.concat(PosDlgConst.Unit).concat("\n");// 收银
////
////                    tContent = tContent.concat(PosDlgConst.Change);
////                    tContent = tContent.concat(tfdChange.getText());
////                    tContent = tContent.concat(PosDlgConst.Unit);// 找零
////
////                    tContent =
////                            tContent.concat("\n\n      ").concat(
////                                    (String) CustOpts.custOps.getValue(PosDlgConst.Thankword));
////                    tContent = tContent.concat("\n\n");
////
////                    Object tEncodType = CustOpts.custOps.getValue(PosDlgConst.EncodeStyle);
////                    if (tEncodType == null)
////                        tEncodType = "GBK";
////                    if (!Charset.isSupported(tEncodType.toString()))
////                        return;
////                    BufferedWriter tWriter =
////                            new BufferedWriter(new OutputStreamWriter(outputStream, tEncodType.toString()));
////                    tWriter.write(tContent);
////                    tWriter.close();

        			return "OK";
//                //}
//            }
//
//			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.PrinterError());
//            return null;
//        } catch (Exception e) {
//			ErrorUtil.write(e);
//			L.e("LPT printing", "Error when printing content to LPT.", e);
//			return null;
//        }finally {
//        	if(outputStream != null) {
//        		try {
//        			outputStream.close();
//        		}catch(Exception exp) {
//        			System.out.println(exp);
//					L.e("serial2", "Error when trying to close outputString of serial port", exp);
//        		}
//        	}
//        	if(tSerialPort != null) {
//        		try {
//        			tSerialPort.close();
//        		}catch(Exception e) {
//        			System.out.println(e);
//					L.e("serial2", "Error when trying to close serial port", e);
//        		}
//        	}
//        }
    }

	static void sendContentOutThroughStream(String sndMsg, OutputStream outputStream, String ip)
			throws IOException {
		if("Command.BEEP".equals(sndMsg)) {
			outputStream.write(Command.BEEP);
		}else if("BigFont".equals(sndMsg)) {
	
	        String font = (String)CustOpts.custOps.getValue("serialfont");
	        if(font ==  null || font.length() < 1) {
	            font = (String)CustOpts.custOps.getValue("font");
	        }
	        
			if (font == null || font.length() < 1) {
				outputStream.write(Command.GS_ExclamationMark);
			} else {
				// default: "27, 33, 48" because it works for both thermal and non-thermal
				String[] pieces = font.split(",");
				if (pieces.length != 3) {
					outputStream.write(Command.GS_ExclamationMark);
				} else {
					for (int i = 0; i < 3; i++) {
						Command.GS_ExclamationMark[i] = Integer.valueOf(pieces[i].trim()).byteValue();
					}
					outputStream.write(Command.GS_ExclamationMark);
				}
			}
		}else if("NormalFont".equals(sndMsg)) {
			outputStream.write(Command.ESC_ExclamationMark);
		}else if(CUT.equals(sndMsg.trim())){
			// cut the paper.
			outputStream.write(Command.GS_V_m_n);
		}else {
			// code can be customized
			String charset = BarOption.getCharset();
			 if(sndMsg != null) {
			    byte[] send;
			    try {
			        send = sndMsg.getBytes(charset);
			    } catch (UnsupportedEncodingException var5) {
			    	ErrorUtil.write("Can not conver with code:" + charset);
			        send = sndMsg.getBytes();
			    	ErrorUtil.write("content to print will be:" + send);
			    }
			    outputStream.write(send);
			}
		}
		outputStream.flush();
	}

	static void initContentForEachRef(String wholRefContent, List<String> numeroRefs, List<String> dateRefs, List<List<String>> oldMoneys) {
		String[] strs = wholRefContent.split(REF_TO);
		for (int i = 0; i < strs.length; i++) {
			//for each ref string, we need to init an empty list for holding the old moneys(subtotal,tpq,tvq of old bill)
			if(oldMoneys.size() < i + 1) {
				List<String> list = new ArrayList<String>();
				list.add(null);
				list.add(null);
				list.add(null);
				list.add(null);
				oldMoneys.add(list);
			}
			//then fill the content into the new created money list and relevant position of umberoRef list and dateRefList. 
			String strLeft = initOldMoneys(strs[i], oldMoneys.get(i));
			//fill the dateRefs
			strLeft = initDateRef(strLeft, dateRefs);
			//fill the nueroRefs
			numeroRefs.add(strLeft);
		}
	}

	static String initOldMoneys(String singleRefContent, List<String> oldMoneys) {
		String oldsubtotal = null;
		String oldGST = null;
		String oldQST = null;
		String oldTotal = null;
		
		int p = singleRefContent.indexOf(OLD_SUBTOTAL);			//add another check to see if the oldSubtotal are different.
		if(p > 0) {
			oldsubtotal = singleRefContent.substring(p + OLD_SUBTOTAL.length()).trim();
			singleRefContent = singleRefContent.substring(0, p).trim();
			p = oldsubtotal.indexOf(OLD_GST);
			if(p > 0) {
				oldGST = oldsubtotal.substring(p + OLD_GST.length()).trim();
				oldsubtotal = oldsubtotal.substring(0, p).trim();
				p = oldGST.indexOf(OLD_QST);
				if(p > 0) {
					oldQST = oldGST.substring(p + OLD_QST.length()).trim();
					oldGST = oldGST.substring(0, p).trim();
					p = oldQST.indexOf(OLD_TOTAL);
					if(p > 0) {
						oldTotal = oldQST.substring(p + OLD_TOTAL.length()).trim();
						oldQST = oldQST.substring(0, p).trim();
						p = oldTotal.indexOf(REF_TO);
						if(p > 0) {
							oldTotal = oldTotal.substring(0, p).trim();
						}
					}
				}
			}
			
			//in case at the end there's an other *ref to(e.g first print added *ref. then when modified, a *subtotal was
			//added, then print bill again, another *ref to will be added at the end.
			p = oldsubtotal.indexOf(REF_TO);
			if(p > 0) {
				singleRefContent = oldsubtotal.substring(p + REF_TO.length()).trim();
				oldsubtotal = oldsubtotal.substring(0, p).trim();
			}
		}
		oldMoneys.clear();
		oldMoneys.add(oldsubtotal);
		oldMoneys.add(oldGST);
		oldMoneys.add(oldQST);
		oldMoneys.add(oldTotal);
		
		p = singleRefContent.lastIndexOf(REF_TO);
		if(p > 0) {
			singleRefContent = singleRefContent.substring(p + REF_TO.length());
		}
		return singleRefContent;
	}
	
	private static String initDateRef(String strLeft, List<String> dateRefs){
		int p = strLeft.lastIndexOf(DATE_REF);			//add another check to see if the oldSubtotal are different.
		if(p > 0) {
			String dateRefStr = strLeft.substring(p + DATE_REF.length()).trim();
			dateRefs.add(dateRefStr);
			strLeft = strLeft.substring(0, p).trim();
		}
		
		p = strLeft.lastIndexOf(REF_TO);
		if(p > 0) {
			strLeft = strLeft.substring(p + REF_TO.length());
		}
		return strLeft;
	}
	
	static byte[] buildSerialLabelContent(List<String> sndMsg) {
		//the contents could be composed by :1/Command.BEEP 2/BigFont 3/label message 4/cut 5/normal font.
		//while when sending to serial device, the sndMsg could only be content. and length will always be 1.
		//we will make the content into 
		byte[] formattedContent = null;
		
		StringBuilder printContent = new StringBuilder();
		for (int i = 0; i < sndMsg.size(); i++) {					
			printContent.append(sndMsg.get(i));
		}
		
		try {
			formattedContent = printContent.toString().getBytes("ASCII");
		}catch(UnsupportedEncodingException e) {
			ErrorUtil.write(e);
		}
		
		return formattedContent;
	}

	static String printSerialFormattedContent(Path path, byte[] contentToWirteToFile) {
		//write content to file.
		try {
	    	Files.write(path, contentToWirteToFile);
	    } catch (IOException e) {
	    	ErrorUtil.write(e);
	    	return null;
	    }
		//set font
		String font = (String)CustOpts.custOps.getValue("serialfont");
	    if(font ==  null || font.length() < 1) {
	        font = (String)CustOpts.custOps.getValue("font");
	    }
	    byte[] fontCmd = null;
		if (font == null || font.length() < 1) {
			fontCmd = Command.GS_ExclamationMark;
		} else {
			// default: "27, 33, 48" because it works for both thermal and non-thermal
			String[] pieces = font.split(",");
			if (pieces.length != 3) {
				fontCmd = Command.GS_ExclamationMark;
			} else {
				for (int i = 0; i < 3; i++) {
					Command.GS_ExclamationMark[i] = Integer.valueOf(pieces[i].trim()).byteValue();
				}
				fontCmd = Command.GS_ExclamationMark;
			}
		}
		printThroughOSdriver(getSerialCommandFilePath("serialBigFontCommand.xml", fontCmd), false);
		//print the file content out.
		boolean isSuccess = printThroughOSdriver(path, null, null, !BarOption.isDebugMode());

		//print cut paper command.
		if(isSuccess) {
			isSuccess = printThroughOSdriver(getSerialCommandFilePath("serialCutCommand.xml", Command.GS_V_m_n), null, null, false);
		}

		return "OK";
	}
//
//      L.d("PrintService", "start to translate selection into ipContent for printing.");
//      String ip = getIPofNonEmptyIpContentMapEntry();
//      if(ip != null){
//          L.d("PrintService", "ipContent not empty, means last print job not finished yet! returning not success flag.");
//          return ip;                     //未打印完毕
//      }
//      if(checkErrorPrinterList()){
//          BarFrame.setStatusMes("PRINTING...");
//      }
//      popKitchenBillIdx();
//
//      Double priceOfBill = saveSaleRecToDB(isCancel);                              //save to db
//
//      if(!StringUtils.isBlank(serverip)){                     //send to server
//          sendToServer(serverip);
//      }
//
//      if (manageDishesIntoMapAndWaitingForPrint(isCancel, priceOfBill)) { //send to printer
//          return SUCCESS_STR;
//      }else {
//          return ERROR_STR;
//      }
	private static boolean checkErrorPrinterList() {
	    StringBuilder ipStr = new StringBuilder();
	    for(String ip : errorPrinterList){
	        ipStr.append(",");
	        ipStr.append(ip);
	    }
	    if(ipStr.length() > 1) {
	        BarFrame.setStatusMes("PRINTER ERROR, restart printer " + ipStr.substring(1) + ", and restart this app");
	        return false;
	    }
	    return true;
	}

	//customer info is saved in USER table.
	static String findClientInfoByTableName(String tableName) {
		StringBuilder sql = new StringBuilder("select e.NName, e.phone, e.address from dining_table as d, contact as e where d.displayIdx = e._id and d.name = '").append(tableName).append("'");
		try {
			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            while (rs.moveToNext()) {
            	StringBuilder strFR = new StringBuilder(DBUtil.getCursorString(rs,"NName")).append(", ")
            			.append(DBUtil.getCursorString(rs,"Phone")).append("\n").append(DBUtil.getCursorString(rs,"Address"));
                return strFR.toString();
            }
		}catch(Exception exp) {
			L.e("PrintUtil findClientInfoByTableName ", "exception when fetching client Info" + sql, exp);
		}
		return "";
	}

	private static byte[] getImageBytes_chatGPT(Bitmap image) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return stream.toByteArray();
	}
	public static byte[] getImageBytes(Bitmap image) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		// 初始化打印机
		stream.write(Command.ESC_Init);

		int width = image.getWidth();
		int height = image.getHeight();

		// 打印图像命令
		stream.write(Command.GS_k_m_v_r_nL_nH);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x += 8) {
				int colorByte = 0;
				for (int pixelOffset = 0; pixelOffset < 8; pixelOffset++) {
					if (x + pixelOffset < width) {
						int pixel = image.getPixel(x + pixelOffset, y);
						int red = (pixel >> 16) & 0xFF;
						int green = (pixel >> 8) & 0xFF;
						int blue = pixel & 0xFF;
						int luminance = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
						if (luminance < 127) {
							colorByte += (1 << (7 - pixelOffset));
						}
					}
				}
				stream.write(colorByte);
			}
			stream.write(0x0A); // 打印并走纸一行
		}

		return stream.toByteArray();
	}
}
