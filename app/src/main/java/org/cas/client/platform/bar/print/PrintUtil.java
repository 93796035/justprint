package org.cas.client.platform.bar.print;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.cascustomize.CustOpts;
import org.cas.client.platform.casutil.CASUtility;
import org.cas.client.platform.casutil.ErrorUtil;
import org.cas.client.platform.casutil.L;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	private static final byte[] INIT_PRINTER = {0x1B, 0x40}; // 初始化打印机
	private static final byte[] SELECT_QR_MODEL = {0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00}; // 选择QR码模型2
	private static final byte[] SET_QR_SIZE = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x0C}; // 设置QR码大小
	private static final byte[] SET_QR_ERROR_CORRECTION = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x30}; // 设置错误校正级别
	private static final byte[] PRINT_QR_CODE = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30}; // 打印QR码

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

	static int checkPrinter(String printerName) {
		int p = printerName.indexOf("192.168.");  //local printer
		return p < 0 ? printerName.indexOf("10.") : p;
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

	public static CompletableFuture<String> doWebSocketPrint(String ip, List<String> sndMsg, Bitmap image, String responseFromWebMev) {
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
					return null;//TODO: shouldn't return null, should throw an exception or show message to user.
				}

				socket = new Socket(ip, 9100);
				outputStream = socket.getOutputStream();
				//main content


				Paint paint = new Paint();
				paint.setColor(Color.BLACK);
				paint.setTextSize(24);
				paint.setAntiAlias(true);

				// 计算图像宽度和高度
				int maxWidth = (int) paint.measureText("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
				List<String> lines2 = Arrays.asList(responseFromWebMev.split("\n"));
				int totalHeight = (sndMsg.size() + lines2.size() + 2) * 30; // 每行30px
				if (image != null) {
					totalHeight += image.getHeight();
				}

				Bitmap combinedImage = Bitmap.createBitmap(maxWidth, totalHeight, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(combinedImage);
				canvas.drawColor(Color.WHITE);

				int y = drawText(sndMsg, canvas, paint, maxWidth, 0);
				if (image != null) {
					canvas.drawBitmap(image, (maxWidth - image.getWidth()) / 2, y, null);
					y += image.getHeight();
				}
				drawText(lines2, canvas, paint, maxWidth, y);

				// 保存为临时文件
				File tempFile = new File("/storage/emulated/0/Android/data/com.just.print/files/", "combinedPrint.png");
				FileOutputStream fos = new FileOutputStream(tempFile);
				combinedImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();


				for (String msg : sndMsg) {
					if (msg.contains(REF_TO) && msg.substring(msg.indexOf(REF_TO) + REF_TO.length()).equals(sndMsg.get(0).substring(1))) {
						continue;
					}
					sendContentOutThroughStream(msg, outputStream, ip);
				}
				//QRCode.
				if(image != null) {
//					outputStream.write(INIT_PRINTER);	// 初始化打印机
//					outputStream.write(SELECT_QR_MODEL);			// 选择QR码模型
//					outputStream.write(SET_QR_SIZE);	// 设置QR码大小
//					outputStream.write(SET_QR_ERROR_CORRECTION);	// 设置错误校正级别

					// 存储QR码数据
					byte[] qrDataBytes = convertImageToEscPosCommand(combinedImage);
//					int qrDataLength = qrDataBytes.length + 3;
//					byte[] storeQrDataCommand = {0x1D, 0x28, 0x6B, (byte) (qrDataLength % 256), (byte) (qrDataLength / 256), 0x31, 0x50, 0x30};
//					outputStream.write(storeQrDataCommand);
					outputStream.write(qrDataBytes);

//					// 打印QR码
//					outputStream.write(PRINT_QR_CODE);
//
//					// 结束打印
//					outputStream.write("\n\n".getBytes());
					//////////////////////////
				}
				if(responseFromWebMev != null){
					List<String> lastPart = Arrays.asList(responseFromWebMev.split("\n"));
					for (String msg : lastPart) {
						sendContentOutThroughStream(msg, outputStream, ip);
					}
					sendContentOutThroughStream(CUT, outputStream, ip);
				}
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
	private static int drawText(List<String> lines, Canvas canvas, Paint paint, int maxWidth, int startY) {
		int y = 20; //@NOTE can not print from 0, because seems the first line was printed out of the area.(can not be seen)
		if(lines.size() == 0) {
			return y;
		}
		y = startY;
		for (String msg : lines) {
			if (msg.contains(REF_TO) && msg.substring(msg.indexOf(REF_TO) + REF_TO.length()).equals(lines.get(0).substring(1))) {
				continue;
			}
			float textWidth = paint.measureText(msg);
			float x = (maxWidth - textWidth) / 2;
			canvas.drawText(msg, x, y, paint);
			y += 30;
		}
		return y;
	}
//	private static int drawFisrtPart(List<String> lines1, Graphics2D g, int maxWidth) {
//		int y = 20; //@NOTE can not print from 0, because seems the first line was printed out of the area.(can not be seen)
//		if(lines1.size() == 0) {
//			return y;
//		}
//		boolean startToLeftAline = false;
//		for (int i = 0; i < lines1.size() - 1; i++) {
//			String line = lines1.get(i).trim();
//			int startP = 0;
//			if(!startToLeftAline && line.equals(BarUtil.getSeperatorLine(0))) {
//				startToLeftAline = true;
//				line += line.substring(0, 30);
//			}
//			if(!startToLeftAline) {
//				startP = (maxWidth - g.getFontMetrics().stringWidth(line)) / 2;
//			}
//
//			// Check for consecutive spaces when startP is zero
//			if (startP == 0) {
//				int lastSpaceIndex = line.lastIndexOf("  ");
//				if (lastSpaceIndex != -1) {
//					String leftPart = line.substring(0, lastSpaceIndex);
//					String rightPart = line.substring(lastSpaceIndex).trim();
//					g.drawString(leftPart, 0, y);
//					g.drawString(rightPart, maxWidth - g.getFontMetrics().stringWidth(rightPart), y);
//					y += g.getFontMetrics().getHeight();
//					continue;
//				}
//			}
//
//			g.drawString(line, startP, y);
//			y += g.getFontMetrics().getHeight();
//		}
//		String lastLine = lines1.get(lines1.size() - 1).trim();
//		g.drawString(lastLine, (maxWidth - g.getFontMetrics().stringWidth(lastLine)) / 2, y);
//		return y;
//	}
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
				if(!sndMsg.endsWith("\n"))
					sndMsg = sndMsg + "\n";
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

	public static byte[] convertImageToEscPosCommand(Bitmap image) {
		int width = image.getWidth();
		int height = image.getHeight();

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write(0x1B);// 设置初始化命令
		stream.write('@');
		stream.write(0x1B);// 设置图片模式
		stream.write(0x33);
		stream.write(0x30); // 设置行间距

		for (int y = 0; y < height; y += 24) {
			stream.write(0x1B);
			stream.write(0x2A);
			stream.write(0x21); // 选择位图模式，24点密度
			stream.write((byte) (width % 256)); // 图片宽度低位
			stream.write((byte) (width / 256)); // 图片宽度高位

			for (int x = 0; x < width; x++) {
				for (int k = 0; k < 3; k++) {
					byte slice = 0;
					for (int b = 0; b < 8; b++) {
						int yy = y + (k * 8) + b;
						if (yy >= height) {
							continue;
						}
						int pixel = image.getPixel(x, yy);
						int gray = (pixel & 0xFF); // 只考虑一个通道
						if (gray < 128) {
							slice |= (1 << (7 - b));
						}
					}
					stream.write(slice);
				}
			}
			stream.write(0x0A); // 换行
		}
		stream.write(0x1B);// 重置行间距
		stream.write(0x32);
		return stream.toByteArray();
	}

	public static byte[] getImageBytes(Bitmap image) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write(Command.ESC_Init);// 初始化打印机
		int width = image.getWidth();
		int height = image.getHeight();

		stream.write(Command.GS_k_m_v_r_nL_nH);		// 打印图像命令

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
