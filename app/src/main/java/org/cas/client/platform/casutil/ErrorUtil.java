/*
 * Created on 2004-7-8
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.cas.client.platform.casutil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code
 * and Comments
 */
public class ErrorUtil extends ByteArrayOutputStream {
    protected static final boolean ENCODE = false;

    private static int saved; // The error log save flag : 0 - Not saved, 1 - Saved, 2 - Too many exceptions.
                              // If the current error log length >= 64K, not write any text.
    private static int start = -1;

    /** write message */
    public static void write(
            String message) {
        // Openned for testing on 18/2
        write(null, message, 0, 0, 1);
    }

    /**
     * Write the error message to error log file.
     * 
     * @param e
     *            the exception.
     */
    public static void write(
            Exception e) {
        // Disable register error log popup dialog.2002-3-27
        write(null, e);
    }

    /**
     * Write the error message to error log file, and popup a dialog.
     * 
     * @param id
     *            the programmer group and id.
     * @param e
     *            the exception.
     */
    public static void write(
            String id,
            Exception e) {
        write(id, e, 0, 0, 0);
    }

    /**
     * write the buffer.
     * 
     * @param buf
     *            A byte array
     * @param off
     *            Offset from which to start taking bytes
     * @param len
     *            Number of bytes to write
     */
    @Override
    public void write(
            byte[] buf,
            int off,
            int len) {
        // Start of Optimization,for stack & memory
        String id = "0";
        int stack = 0;
        boolean found = false;
        int type = analyze(buf, off, off + len);
        if (type == 0) {
            found = true;
            stack = 0;
        } else if (type == 1) {
            type = found ? -1 : 0;
            stack = 0;
            // Start of Optimization,uncatched exception（02/18/2002）－by WuLiangqiao（User084）
            found = true;
            // End of Optimization.
        } else if (type == 2) {
            if (!found) {
                type = -1;
            }
            found = false;
            stack = 0;
        } else if (type == 3) {
            type = found ? -1 : 0;
            found = false;
            stack = 99;
        } else if (type == 4) {
            type = 2;
            id = "memory";
            found = false;
            stack = 0;
        } else if (stack > 0) {
            if (--stack == 0) {
                type = 2;
                id = "stack";
                stack = -1;
            }
        } else if (stack < 0) {
            return;
        }
        for (int i = off, j = len; j > 10; i++, j--) {
            if (buf[i] == 'a' && buf[i + 1] == 't' && buf[i + 2] == ' ') {
                process(buf, i + 3, off + len);
                break;
            }
        }

        write(type == 2 ? id : null, buf, off, len, type);
        // End of Optimization.
    }

    private static void write(
            String id,
            Object e,
            int off,
            int count,
            int type) {
        // System.out.print(e);
        RandomAccessFile rf = null;
        Log pw = null;
        try {
            File parent = new File(CASUtility.getPIMDirPath());
            boolean exists = parent.exists();
            if (!exists || !parent.isDirectory()) {
                if (exists) {
                    parent.mkdir();
                } else {
                    parent.mkdirs();
                }
            }

            rf =
                    new RandomAccessFile(CASUtility.getPIMDirPath().concat(System.getProperty("file.separator"))
                            .concat("pim_log.txt"), "rw");
            long size = rf.length();
            if (start != 0 && size > 0xC000) {
                int remain = Math.max(start > 0 ? (int) size - start : 0, 0x7FE0);
                // Start of Optimization,（01/09/2002）－by WuLiangqiao(User084)
                int offs = (int) size - remain;
                if (start > 0) {
                    if (offs > start) {
                        offs = start;
                    }
                    start -= offs;
                }
                remain = (int) size - offs;
                rf.seek(offs);
                byte[] buffer = new byte[remain];
                rf.read(buffer);
                rf.seek(0);
                if (start != 0) {
                    rf.writeBytes("Some old log messages are deleted.\r\n");
                    int bytes = (int) rf.getFilePointer();
                    if (start > 0) {
                        start += bytes;
                    }
                    remain += bytes;
                }
                rf.write(buffer);
                buffer = null;
                rf.setLength(size = remain);
            } else {
                if (size >= 0x10000) {
                    saved = 2;
                }
                rf.seek(size);
            }
            Date date = type == 0 || saved == 0 ? new Date() : null;
            if (saved == 0) {
                start = (int) size;
                // End of Optimization
                saved = 1;
                rf.writeShort(0xD0A);
                for (int i = 80; i-- > 0;) {
                    rf.writeByte('-');
                }
                // Start of Optimization,add version（01/11/2002）－by WuLiangqiao（User084）
                rf.writeBytes(new SimpleDateFormat("'\r\n'yyyy.MM.dd HH:mm:ss'  " + "PIM" + "\r\n'").format(date));
                // //End of Optimization.
            }
            if (type == 0) {
                rf.writeBytes(new SimpleDateFormat("'\r\nException occurs:' yyyy.MM.dd HH:mm:ss'\r\n'").format(date));
            }
            if (e instanceof Exception) {
                // 用于写 Errorlog，不可以注释掉
                ((Exception) e).printStackTrace(pw = new Log(rf));
            } else if (e instanceof byte[]) {
                rf.write((byte[]) e, off, count);
            }
            // Openned for testing on 2/18
            else if (e instanceof String) {
                rf.writeBytes((String) e);
                rf.writeShort(0xD0A);
            }
            // The log length has exceeded 64K.
            if (saved == 2) {
                rf.writeBytes("\r\nToo many exceptions.\r\n");
            }
        } catch (Exception ex) {
            // Your system has error
            // Cannot process this exception, otherwise will be cycle call.
        }
        try {
            if (pw != null) {
                pw.close();
            } else if (rf != null) {
                rf.close();
            }
        } catch (Exception exp) {
            // Cannot process this exception
            // Otherwise will be cycle call.
        }
        // End of Optimization.
        // End of Optimization.
    }

    /*
     * return the type -1 = normal, 0 - first, 1 - exception, 2 - run
     */
    private int analyze(
            byte[] buf,
            int start,
            int end) {
        if (end - start > 40) {
            if (equals("Exception occurred", buf, start, start + 18)) {
                return 0;
            }
        }
        int next = next(buf, start, end);
        if (next < 0 || buf[next] != '.') {
            return -1;
        }
        int type = 1;
        int count = 1;
        while (true) {
            start = next + 1;
            next = next(buf, start, end);
            count++;
            // Start of Optimization, judge（01/11/2002）－by WuLiangqiao（User084）
            // Start of Optimization,for stack & memory（01/11/2002）－by WuLiangqiao（User084）
            int token;
            if (next < 0 || next >= end - 1 || (token = buf[next]) == ':') {
                if (next < 0) {
                    next = end;
                }
                if (count == 3 && type == 1) {
                    if (equals("StackOverflowError", buf, start, next)) {
                        return 3;
                    }
                    if (equals("OutOfMemoryError", buf, start, next)) {
                        return 4;
                    }
                }
                return count < 3 ? -1 : type;
            }
            // End of Optimization.
            // End of Optimization.
            if (token == '(') {
                return equals("run", buf, start, next) ? 2 : -1;
            } else if (token != '.') {
                type = -1;
            }
        }
    }

    private static void process(
            byte[] buf,
            int start,
            int end) {
        if (ENCODE) {
            int next = next(buf, start, end);
            if (next < 0) {
                return;
            }
            int type = 1;
            if (equals("org", buf, start, next)) {
                type = 3;
            } else if (equals("sun", buf, start, next)) {
                type = 4;
            }
            if (equals("java", buf, start, next)) {
                type = 2;
            } else if (!equals("javax", buf, start, next)) {
                return;
            }
            if ((next = next(buf, next + 1, end)) < 0) {
                return;
            }
            set(type, buf, start, next);
            boolean ev = false;
            while (true) {
                start = next + 1;
                next = next(buf, start, end);
                if (next < 0 || next >= end - 1) {
                    return;
                }
                int token = buf[next];
                if (token == '(') {
                    token = ev ? -1 : 1;
                } else if (token == ':' || token == ')') {
                    return;
                } else {
                    token = buf[start];
                    if (token == 'E' && buf[start + 1] == 'v') {
                        token = 0;
                        ev = true;
                    } else {
                        token = token > 'Z' ? -2 : type;
                    }
                }
                set(buf, start, next, token);
            }
        }
    }

    private static int next(
            byte[] buf,
            int start,
            int end) {
        while (start < end) {
            int ch = buf[start];
            if (ch == '.' || ch == '$' || ch == '(' || ch == ':') {
                return start;
            }
            start++;
        }
        return -1;
    }

    private static boolean equals(
            String s,
            byte[] buf,
            int start,
            int end) {
        int length = s.length();
        if (length == end - start) {
            for (end = 0; end < length; end++) {
                if (s.charAt(end) != buf[start++]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static void set(
            int kind,
            byte[] buf,
            int start,
            int end) {
        for (int i = 0; start < end; i++) {
            buf[start++] = i == 0 ? (byte) 'o' : (byte) '0';
            if (i == kind) {
                i = -1;
            }
        }
    }

    private static void set(
            byte[] buf,
            int start,
            int end,
            int type) {
        for (int i = 0, last = 0, mask = 0; start < end; start++, i++) {
            // Comment the following code
            if (ENCODE) {
                int ch = buf[start];
                if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') {
                    int temp = ch & 31;
                    if (type > 0) {
                        if (mask == 0) {
                            mask = type;
                        }
                        temp += mask;
                        if (mask > type) {
                            mask = -mask;
                        } else {
                            mask += type;
                        }
                    } else if (type >= -1) {
                        temp = (last ^ ch ^ 31) & 31;
                        if (type != 0 && ((temp <<= 1) > 31)) {
                            temp -= 31;
                        }
                    } else {
                        if ((temp <<= 1) > 31) {
                            temp -= 31;
                        }
                    }
                    last = ch;
                    buf[start] = (byte) ((ch & 0x60 | ((i & 3) == 0 ? 1 : 0x21)) + (temp + 25) % 26);
                }
            }
        }
    }

    // The class is used for encode the log file
    private static class Log extends PrintStream {
        Log(RandomAccessFile rf) throws IOException {
            super(new FileOutputStream(rf.getFD()));
        }

        /**
         * write the buffer.
         * 
         * @param buf
         *            A byte array
         * @param off
         *            Offset from which to start taking bytes
         * @param len
         *            Number of bytes to write
         */
        @Override
        public void write(
                byte[] buf,
                int off,
                int len) {
            System.out.print(new String(buf, off, len));
            for (int i = off, j = len; j > 10; i++, j--) {
                // Comment the following codes
                if (ENCODE) {
                    if (buf[i] == 'a' && buf[i + 1] == 't' && buf[i + 2] == ' ') {
                        ErrorUtil.process(buf, i + 3, off + len);
                        break;
                    }
                }
            }
            super.write(buf, off, len);
        }
    }
}
