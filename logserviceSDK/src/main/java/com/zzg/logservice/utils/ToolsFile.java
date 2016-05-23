package com.zzg.logservice.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zzg.logservice.config.AppConstant;
import com.zzg.logservice.service.LogService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 全局共用的对文件操作的方法
 * 
 * @author Abelzzg
 */
public class ToolsFile {

	private static int BUFFER_SIZE = 1024 * 8;

	/**
	 * 复制文件
	 * 
	 * @param fromF
	 *            源文件
	 * @param toF
	 *            目的文件
	 */
	public static void copyFile(File fromF, File toF) {
		FileInputStream fileInputStream = null;
		FileOutputStream fileOutputStream = null;
		try {
			if (!toF.exists()) {
				toF.createNewFile();
			}
			fileInputStream = new FileInputStream(fromF);
			fileOutputStream = new FileOutputStream(toF);
			byte[] buffer = new byte[BUFFER_SIZE];
			for (int bytesRead = 0; (bytesRead = fileInputStream.read(buffer,
					0, buffer.length)) != -1;) {
				fileOutputStream.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileInputStream != null)
					fileInputStream.close();
				if (fileOutputStream != null)
					fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 复制一个目录或文件
	 * 
	 * @param from
	 *            需要复制的目录 例如：/home/from
	 * @param to
	 *            复制到的目录 例如：/home/to
	 * @param isCover
	 *            是否覆盖
	 */
	public static void copy(String from, String to, boolean isCover) {
		File fromF = new File(from);
		File toF = new File(to + "/" + fromF.getName());
		copyR(from, toF.getAbsolutePath(), isCover);
	}

	public static void makeDir(File dir) {
		if (!dir.getParentFile().exists()) {
			makeDir(dir.getParentFile());
		}
		dir.mkdir();
	}

	public static JsonObject readFile(File file) {
		BufferedReader reader = null;
		String laststr = "";
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
				laststr = laststr + tempString;
				line++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		if (laststr == "") {
			laststr = "{}";
		}
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject) jsonParser.parse(laststr);
		return jsonObject;
	}

	/**
	 * 读取文件
	 * 
	 * @param path
	 * @return
	 */
	public static String readFile(String path) {
		File file = new File(path);
		if (!isFileExit(path)) {
			makeDir(file.getParentFile());
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		BufferedReader reader = null;
		String laststr = "";
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
				laststr = laststr + tempString;
				line++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		if (laststr == "") {
			laststr = "{}";
		}
		return laststr;
	}

	public synchronized static JsonArray readAllLog(String logDir) {
		// 读取dir下的所有log文件
		JsonArray jsons = new JsonArray();
		File f = new File(logDir);
		if (!f.exists()) {
			return null;
		}
		File fa[] = f.listFiles();
		for (int i = 0; i < fa.length; i++) {
			File fs = fa[i];
			if (fs.isDirectory()) {
			} else {
				if (fs.getName().contains("log.json")
						&& !fs.getName().equals(LogService.logName)) {
					jsons.add(readFile(fs));
				}
			}
		}
		return jsons;
	}

	public static List<File> searchFiles(String logDir) {
		List<File> files = new ArrayList<File>();
		File f = new File(logDir);
		if (!f.exists()) {
			return null;
		}
		File fa[] = f.listFiles();
		for (int i = 0; i < fa.length; i++) {
			File fs = fa[i];
			if (fs.isDirectory()) {
			} else {
				if (fs.getName().contains("log.json")) {
					files.add(fs);
				}
			}
		}
		return files;
	}

	// 把json格式的字符串写到文件
	public static void writeFile(String filePath, String sets)
			throws IOException {
		FileWriter fw = new FileWriter(filePath);
		PrintWriter out = new PrintWriter(fw);
		out.write(sets);
		out.println();
		fw.close();
		out.close();
	}

	public static void deleteFile() {
		List<File> files = ToolsFile.searchFiles(AppConstant.logDir);// 查找文件
		for (int i = 0; i < files.size(); i++) {
			if (!files.get(i).getName().equals(LogService.logName)) {// 过滤files
				ToolsFile.deleteFile(files.get(i));// 删掉文件
			}
		}
	}

	/**
	 * @param from
	 *            源文件文件名
	 * @param to
	 *            目的文件文件名
	 * @param isCover
	 *            是否覆盖
	 */
	private static void copyR(String from, String to, boolean isCover) {
		File fromF = new File(from);
		if (fromF.isDirectory()) {
			File toF = new File(to);
			toF.mkdirs();
			File[] files = fromF.listFiles();
			for (File f : files) {
				try {
					File toTmpF = new File(toF.getAbsolutePath() + "/"
							+ f.getName());
					copyR(f.getAbsolutePath(), toTmpF.getAbsolutePath(),
							isCover);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			File toF = new File(to);
			if (!toF.exists()) {
				try {
					toF.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				copyFile(fromF, toF);
			} else {
				if (isCover) {
					try {
						toF.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
					copyFile(fromF, toF);
				}
			}

		}
	}

	/**
	 * 拷贝assets下的文件
	 * 
	 * @param assetFilePath
	 *            asset中的文件路径
	 * @param to
	 *            拷贝后的文件路径
	 */
	public static void copyAssetFile(String assetFilePath, String to) {
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		try {
			inputStream = LogService.globalContext.getAssets().open(
					assetFilePath);

			File toDir = new File(to);
			toDir.mkdirs();
			File toFile = new File(
					toDir.getAbsolutePath()
							+ "/"
							+ assetFilePath.substring(assetFilePath
									.lastIndexOf("/") + 1));
			fileOutputStream = new FileOutputStream(toFile);

			byte[] buffer = new byte[BUFFER_SIZE];
			for (int bytesRead = 0; (bytesRead = inputStream.read(buffer, 0,
					buffer.length)) != -1;) {
				fileOutputStream.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
				if (fileOutputStream != null)
					fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 解压zip文件
	 * 
	 * @param srcFileFullName
	 *            需要被解压的文件地址（包括路径+文件名）例如：/home/kx.apk
	 * @param targetPath
	 *            需要解压到的目录 例如： /home/kx
	 */
	public static boolean unzip(String srcFileFullName, String targetPath) {
		try {
			ZipFile zipFile = new ZipFile(srcFileFullName);
			Enumeration<? extends ZipEntry> emu = zipFile.entries();
			while (emu.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) emu.nextElement();
				// 会把目录作为一个file读出一次，所以只建立目录就可以，之下的文件还会被迭代到。
				if (entry.isDirectory()) {
					new File(targetPath + entry.getName()).mkdirs();
					continue;
				}
				BufferedInputStream bis = new BufferedInputStream(
						zipFile.getInputStream(entry));
				File file = new File(targetPath + entry.getName());
				// 加入这个的原因是zipfile读取文件是随机读取的，这就造成可能先读取一个文件
				// 而这个文件所在的目录还没有出现过，所以要建出目录来。
				File parent = file.getParentFile();
				if (parent != null && (!parent.exists())) {
					parent.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos,
						BUFFER_SIZE);

				int count;
				byte data[] = new byte[BUFFER_SIZE];
				while ((count = bis.read(data, 0, BUFFER_SIZE)) != -1) {
					bos.write(data, 0, count);
				}
				bos.flush();
				bos.close();
				bis.close();
			}
			zipFile.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * 压缩文件或目录
	 * 
	 * @param srcPath
	 *            被压缩的文件或目录地址 例如：/home/kx 或 /home/kx/kx.apk
	 * @param targetFileFullName
	 *            压缩过后的文件地址全称（包括路径+文件名）例如： /home/kx.apk
	 */
	public static void zip(String srcPath, String targetFileFullName) {
		ZipOutputStream outputStream = null;
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(targetFileFullName);
			outputStream = new ZipOutputStream(fileOutputStream);
			zip(outputStream, new File(srcPath), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (outputStream != null)
					outputStream.close();
				if (fileOutputStream != null)
					fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 压缩文件
	 * 
	 * @param out
	 *            压缩输出流
	 * @param f
	 *            压缩文件
	 * @param base
	 *            压缩后的文件名
	 */
	private static void zip(ZipOutputStream out, File f, String base) {
		if (f.isDirectory()) {
			File[] fl = f.listFiles();
			base = base.length() == 0 ? "" : base + "/";
			for (int i = 0; i < fl.length; i++) {
				zip(out, fl[i], base + fl[i].getName());
			}
		} else {
			FileInputStream in = null;
			BufferedInputStream bis = null;
			try {
				out.putNextEntry(new ZipEntry(base));
				in = new FileInputStream(f);
				byte[] buffer = new byte[BUFFER_SIZE];
				bis = new BufferedInputStream(in, BUFFER_SIZE);
				int size;
				while ((size = bis.read(buffer)) != -1) {
					out.write(buffer, 0, size);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				try {
					if (in != null)
						in.close();
					if (bis != null)
						bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param filePath
	 * @return
	 */
	public static void deleteFile(String filePath) {
		if (null == filePath || 0 == filePath.length()) {
			return;
		}
		try {
			File file = new File(filePath);
			if (null != file && file.exists()) {
				if (file.isDirectory()) {// 判断是否为文件夹
					File[] fileList = file.listFiles();
					for (int i = 0; i < fileList.length; i++) {
						String path = fileList[i].getPath();
						deleteFile(path);
					}
					file.delete();
				}
				if (file.isFile()) {// 判断是否为文件
					file.delete();// 成功返回true，失败返回false
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param file
	 * @return
	 */
	public static void deleteFile(File file) {
		try {
			if (null != file && file.exists()) {
				if (file.isDirectory()) {// 判断是否为文件夹
					File[] fileList = file.listFiles();
					for (int i = 0; i < fileList.length; i++) {
						String path = fileList[i].getPath();
						deleteFile(path);
					}
					file.delete();
				}
				if (file.isFile()) {// 判断是否为文件
					file.delete();// 成功返回true，失败返回false
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * dirStr1中是否包含所有dirStr1中的内容
	 * 
	 * @param dirStr1
	 * @param dirStr1
	 * @return
	 */
	public static boolean isContain(String dirStr1, String dirStr2) {
		File dir1 = new File(dirStr1);
		File dir2 = new File(dirStr2);
		boolean result = false;
		try {
			result = Arrays.asList(dir1.list()).containsAll(
					Arrays.asList(dir2.list()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Inputstream->byte[]方法
	 * 
	 * @param inputstream
	 * @return
	 * @throws IOException
	 */
	public static final byte[] readBytes(InputStream inputstream)
			throws IOException {
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
		byte bys[] = null;
		try {
			byte abyte0[] = new byte[1024];
			int readlength;
			while (-1 != (readlength = inputstream.read(abyte0))) {
				bytearrayoutputstream.write(abyte0, 0, readlength);
			}
			bys = bytearrayoutputstream.toByteArray();
		} catch (Throwable ex) {
			ex.printStackTrace();
		} finally {
			bytearrayoutputstream.close();
		}
		return bys;
	}

	/**
	 * 取得文件夹大小
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static long getFileSize(File file) {
		if (file == null || !file.exists())
			return 0;
		long size = 0;
		try {
			File flist[] = file.listFiles();
			for (int i = 0; i < flist.length; i++) {
				if (flist[i].isDirectory()) {
					size = size + getFileSize(flist[i]);
				} else {
					size = size + flist[i].length();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return size;
	}

	/**
	 * 转换文件大小
	 * 
	 * @param size
	 * @return
	 */
	public static String formatFileSize(long size) {
		DecimalFormat df = new DecimalFormat("0.00");
		String fileSize = "";
		if (size <= 0) {
			fileSize = "0 KB";
		} else if (size < 1024) {
			fileSize = df.format((double) size) + " B";
		} else if (size < 1048576) {
			fileSize = df.format((double) size / 1024) + " KB";
		} else if (size < 1073741824) {
			fileSize = df.format((double) size / 1048576) + " M";
		} else {
			fileSize = df.format((double) size / 1073741824) + " G";
		}
		return fileSize;
	}

	/**
	 * 判断指定路径下文件是否存在
	 * 
	 * @param filePath
	 *            文件路径
	 * @return 文件是否存在
	 */
	public static boolean isFileExit(String filePath) {
		boolean isExist = false;
		try {
			if (null != filePath) {
				if (new File(filePath).exists()) {
					isExist = true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return isExist;
	}

	/**
	 * 判断某个文件是否存在
	 * 
	 * @param path
	 * @param name
	 * @return
	 */
	public static boolean isFileExit(String path, String name) {
		return isFileExit(path + name);
	}

}
