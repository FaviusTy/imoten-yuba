/*
 * imoten - i mode.net mail tensou(forward)
 * 
 * Copyright (C) 2010 shoozhoo (http://code.google.com/p/imoten/)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */

package immf;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ContentType;
import javax.mail.internet.HeaderTokenizer;
import javax.mail.internet.MimeUtility;

public class Util {
	public static void safeclose(Closeable c){
		if(c!=null){
			try{
				c.close();
			}catch (Exception e) {}
		}
	}
	
	public static String easyEscapeHtml(String s){
		StringBuilder buf = new StringBuilder();
		for(char c : s.toCharArray()){
			if(c=='>'){
				buf.append("&gt;");
			}else if(c=='<'){
				buf.append("&lt;");
			}else if(c=='&'){
				buf.append("&amp;");
			}else if(c=='"'){
				buf.append("&quot;");
			}else{
				buf.append(c);
			}
		}
		return buf.toString();
	}

	
	/*
	 * JavaMail完全解説 のページから使用させていただきました
	 * http://www.sk-jp.com/book/javamail/contents/
	 */
	public static void setFileName(Part part, String filename,
			String charset, String lang)
	throws MessagingException {

		ContentDisposition disposition;
		String[] strings = part.getHeader("Content-Disposition");
		if (strings == null || strings.length < 1) {
			disposition = new ContentDisposition(Part.ATTACHMENT);
		} else {
			disposition = new ContentDisposition(strings[0]);
			disposition.getParameterList().remove("filename");
		}

		part.setHeader("Content-Disposition",
				disposition.toString() +
				encodeParameter("filename", filename, charset, lang));

		ContentType cType;
		strings = part.getHeader("Content-Type");
		if (strings == null || strings.length < 1) {
			cType = new ContentType(part.getDataHandler().getContentType());
		} else {
			cType = new ContentType(strings[0]);
		}

		try {
			// I want to public the MimeUtility#doEncode()!!!
			String mimeString = MimeUtility.encodeWord(filename, charset, "B");
			// cut <CRLF>...
			StringBuffer sb = new StringBuffer();
			int i;
			while ((i = mimeString.indexOf('\r')) != -1) {
				sb.append(mimeString.substring(0, i));
				mimeString = mimeString.substring(i + 2);
			}
			sb.append(mimeString);

			cType.setParameter("name", new String(sb));
		} catch (UnsupportedEncodingException e) {
			throw new MessagingException("Encoding error", e);
		}
		part.setHeader("Content-Type", cType.toString());
	}

	public static String encodeParameter(String name, String value,
			String encoding, String lang) {
		StringBuffer result = new StringBuffer();
		StringBuffer encodedPart = new StringBuffer();

		boolean needWriteCES = !isAllAscii(value);
		boolean CESWasWritten = false;
		boolean encoded;
		boolean needFolding = false;
		int sequenceNo = 0;
		int column;

		while (value.length() > 0) {
			// index of boundary of ascii/non ascii
			int lastIndex;
			boolean isAscii = value.charAt(0) < 0x80;
			for (lastIndex = 1; lastIndex < value.length(); lastIndex++) {
				if (value.charAt(lastIndex) < 0x80) {
					if (!isAscii) break;
				} else {
					if (isAscii) break;
				}
			}
			if (lastIndex != value.length()) needFolding = true;

			RETRY:      while (true) {
				encodedPart.delete(0, encodedPart.length());
				String target = value.substring(0, lastIndex);

				byte[] bytes;
				try {
					if (isAscii) {
						bytes = target.getBytes("us-ascii");
					} else {
						bytes = target.getBytes(encoding);
					}
				} catch (UnsupportedEncodingException e) {
					bytes = target.getBytes(); // use default encoding
					encoding = MimeUtility.mimeCharset(
							MimeUtility.getDefaultJavaCharset());
				}

				encoded = false;
				// It is not strict.
				column = name.length() + 7; // size of " " and "*nn*=" and ";"

				for (int i = 0; i < bytes.length; i++) {
					if (bytes[i] > ' ' && bytes[i] < 'z'
							&& HeaderTokenizer.MIME.indexOf((char)bytes[i]) < 0) {
						encodedPart.append((char)bytes[i]);
						column++;
					} else {
						encoded = true;
						encodedPart.append('%');
						String hex  = Integer.toString(bytes[i] & 0xff, 16);
						if (hex.length() == 1) {
							encodedPart.append('0');
						}
						encodedPart.append(hex);
						column += 3;
					}
					if (column > 76) {
						needFolding = true;
						lastIndex /= 2;
						continue RETRY;
					}
				}

				result.append(";\r\n ").append(name);
				if (needFolding) {
					result.append('*').append(sequenceNo);
					sequenceNo++;
				}
				if (!CESWasWritten && needWriteCES) {
					result.append("*=");
					CESWasWritten = true;
					result.append(encoding).append('\'');
					if (lang != null) result.append(lang);
					result.append('\'');
				} else if (encoded) {
					result.append("*=");
				} else {
					result.append('=');
				}
				result.append(new String(encodedPart));
				value = value.substring(lastIndex);
				break;
			}
		}
		return new String(result);
	}

	/** check if contains only ascii characters in text. */
	public static boolean isAllAscii(String text) {
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) > 0x7f) { // non-ascii
				return false;
			}
		}
		return true;
	}
}
