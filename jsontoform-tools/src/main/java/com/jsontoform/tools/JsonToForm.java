package com.jsontoform.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jsontoform.TypeConstant;
import com.jsontoform.tools.component.GlobalConfigManager;
import com.jsontoform.type.SupportType;
import com.jsontoform.util.StackMessagePrint;
import com.jsontoform.util.StringUtil;

public class JsonToForm {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonToForm.class);

	private static final Gson gson = new Gson();

	/** The Constant lineSeparator. */
	private static final String lineSeparator;

	private static List<String> warnings = new ArrayList<>();

	private static List<String> failedFile = new ArrayList<>();

	private static ByteArrayOutputStream outputStream;

	private static byte[] buffer;

	private static long readFileSpendTime = 0;
	private static long totalSpendTime = 0;

	static {
		String ls = System.getProperty("line.separator"); //$NON-NLS-1$
		if (ls == null) {
			ls = "\n"; //$NON-NLS-1$
		}
		lineSeparator = ls;
	}

	public static void main(String[] args) {
		if (args.length <= 0) {
			LOGGER.error("Please input the global config path...");
			System.exit(1);
		}
		long start = System.currentTimeMillis();
		if (!GlobalConfigManager.getInstance().init(args[0])) {
			LOGGER.error("Init {} manager failed...", GlobalConfigManager.class.getSimpleName());
			System.exit(0);
		}

		translateToForm();
		long end = System.currentTimeMillis();
		totalSpendTime = end - start;
		printExecuteInfo();
		cleanResource();
	}

	private static void translateToForm() {
		String fileFormat = GlobalConfigManager.getInstance().getFileFormat();
		SupportType type = SupportType.parse(fileFormat);
		if (type == null) {
			warnings.add("File format has not support it: " + fileFormat + ", support list: " + SupportType.getSupportList());
			return;
		}
		File fileDir = new File(GlobalConfigManager.getInstance().getJsonFilePath());
		File[] children = fileDir.listFiles();
		if (children == null || children.length == 0) {
			warnings.add("Directory has no file: " + GlobalConfigManager.getInstance().getJsonFilePath());
			return;
		}
		String outPutPath = GlobalConfigManager.getInstance().getOutPutPath();
		File outPutFile = new File(outPutPath);
		if (outPutFile.isFile()) {
			warnings.add("Out path is not incorrect: " + outPutPath);
			return;
		}
		if (!outPutFile.exists()) {
			if (outPutFile.mkdirs()) {
				warnings.add("Create out put directory: " + outPutPath);
			} else {
				warnings.add("Create out put directory failed: " + outPutPath);
				return;
			}
		}
		switch (type) {
			case XLSX:
				translateToExcel(children, outPutPath, fileFormat);
				break;
			case CSV:
				translateToCsv(children, outPutPath, fileFormat);
				break;
			default:
				warnings.add("This file format coming soon: " + fileFormat);
				break;
		}
	}

	private static void translateToExcel(File[] children, String outPutPath, String fileFormat) {
		for (File file : children) {
			if (file.isFile() && file.getName().endsWith(".json")) {
				String filerContent = getFileContent(file);
				String path = outPutPath + String.format("%s.%s", StringUtil.getFilePrefix(file.getName()), fileFormat);
				// 创建Excel文件对象
				XSSFWorkbook workbook = new XSSFWorkbook();
				Sheet sheet = workbook.createSheet("0");
				int rowIndex = 0;
				boolean success = true;
				try {
					List<JsonElement> jsonList = gson.fromJson(filerContent, TypeConstant.LIST_JSON_JE);
					if (jsonList == null) {
						failedFile.add("Check json format: " + file.getName());
						continue;
					}
					for (JsonElement info : jsonList) {
						Map<String, JsonElement> jsonMap = gson.fromJson(info, TypeConstant.MAP_STRING_JE);
						int colIndex = 0;
						boolean initTitle = false;
						Row rowTitle = null;
						if (rowIndex == 0) {
							rowTitle = sheet.createRow(rowIndex);
							rowIndex = 1;
							initTitle = true;
						}
						Row row = sheet.createRow(rowIndex);
						for (Entry<String, JsonElement> entry : jsonMap.entrySet()) {
							JsonElement value = entry.getValue();
							if (initTitle) {
								Cell cell = rowTitle.createCell(colIndex);
								cell.setCellValue(entry.getKey());
							}
							Cell cell = row.createCell(colIndex);
							String cellValue = gson.toJson(value);
							// 去除字符串带双引号
							if (cellValue.startsWith("\"") && cellValue.endsWith("\"")) {
								cellValue = cellValue.substring(1, cellValue.length() - 1);
							}
							cell.setCellValue(cellValue);
							colIndex++;
						}
						rowIndex++;
					}
				} catch (Exception e) {
					success = false;
					failedFile.add("Check json format: " + file.getName() + StackMessagePrint.printErrorTrace(e));
				}
				// 输出Excel文件
				if (success) {
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(path);
						workbook.write(fos);
						fos.flush();
						workbook.close();
					} catch (Exception e) {
						warnings.add("Write out file error: " + path + StackMessagePrint.printErrorTrace(e));
					} finally {
						if (fos != null) {
							try {
								fos.close();
							} catch (IOException e) {
								warnings.add("Close file out put stream file error: " + path + StackMessagePrint.printErrorTrace(e));
							}
						}
					}
				}
			}
		}
	}

	private static void translateToCsv(File[] children, String outPutPath, String fileFormat) {
		String outPutFileCharacterSet = GlobalConfigManager.getInstance().getCsvCharacterSet();
		for (File file : children) {
			if (file.isFile() && file.getName().endsWith(".json")) {
				String filerContent = getFileContent(file);
				String path = outPutPath + String.format("%s.%s", StringUtil.getFilePrefix(file.getName()), fileFormat);
				// 创建Excel文件对象

				int rowIndex = 0;
				FileOutputStream fos = null;
				OutputStreamWriter osw = null;
				try {
					fos = new FileOutputStream(path);
					osw = new OutputStreamWriter(fos, outPutFileCharacterSet);
					CSVFormat csvFormat;
					CSVPrinter csvPrinter = null;
					List<JsonElement> jsonList = gson.fromJson(filerContent, TypeConstant.LIST_JSON_JE);
					if (jsonList == null || jsonList.isEmpty()) {
						failedFile.add("Check json format: " + file.getName());
						continue;
					}
					for (JsonElement info : jsonList) {
						Map<String, JsonElement> jsonMap = gson.fromJson(info, TypeConstant.MAP_STRING_JE);
						boolean initTitle = false;
						String[] heads = null;
						if (rowIndex == 0) {
							initTitle = true;
							heads = new String[jsonMap.size()];
						}
						Object[] rowRecord = new String[jsonMap.size()];
						int colIndex = 0;
						for (Entry<String, JsonElement> entry : jsonMap.entrySet()) {
							JsonElement value = entry.getValue();
							if (initTitle) {
								heads[colIndex] = entry.getKey();
							}
							String cellValue = gson.toJson(value);
							// 去除字符串带双引号
							if (cellValue.startsWith("\"") && cellValue.endsWith("\"")) {
								cellValue = cellValue.substring(1, cellValue.length() - 1);
							}
							rowRecord[colIndex] = cellValue;
							colIndex++;
						}
						if (initTitle) {
							csvFormat = CSVFormat.DEFAULT.withHeader(heads);
							csvPrinter = new CSVPrinter(osw, csvFormat);
						}
						csvPrinter.printRecord(rowRecord);
						rowIndex++;
					}
					// 输出csv文件
					csvPrinter.flush();
					csvPrinter.close();
				} catch (Exception e) {
					failedFile.add("Check json format: " + file.getName() + StackMessagePrint.printErrorTrace(e));
				} finally {
					if (osw != null) {
						try {
							osw.close();
						} catch (IOException e) {
							warnings.add("Close out put stream writer error: " + path + StackMessagePrint.printErrorTrace(e));
						}
					}
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							warnings.add("Close file out put stream file error: " + path + StackMessagePrint.printErrorTrace(e));
						}
					}
				}
			}
		}
	}

	private static String getFileContent(File file) {
		long start = System.currentTimeMillis();
		String fileContent = "";
		FileInputStream fileInputStream = null;
		resetFileReadBuffer();
		try {
			fileInputStream = new FileInputStream(file);
			int len;
			while ((len = fileInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, len);
			}
			byte[] data = outputStream.toByteArray();
			fileContent = new String(data);
		} catch (IOException e) {
			warnings.add(file.getName() + StackMessagePrint.printErrorTrace(e));
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					warnings.add(file.getName() + StackMessagePrint.printErrorTrace(e));
				}
			}
		}
		long end = System.currentTimeMillis();
		readFileSpendTime += end - start;
		return fileContent;
	}

	private static void resetFileReadBuffer() {
		if (outputStream == null) {
			outputStream = new ByteArrayOutputStream();
		} else {
			outputStream.reset();
		}
		if (buffer == null) {
			buffer = new byte[1024];
		} else {
			Arrays.fill(buffer, (byte) 0);
		}
	}

	private static void printExecuteInfo() {
		if (failedFile.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("------------Operate failed file------------");
			sb.append(lineSeparator);
			for (String info : failedFile) {
				sb.append(info);
				sb.append(lineSeparator);
			}
			sb.append("------------Operate failed file------------");
			LOGGER.info(sb.toString());
		}
		if (warnings.size() == 0) {
			LOGGER.info("Execute successful...");
		} else {
			LOGGER.info("Execute failed please try again...");
			StringBuilder sb = new StringBuilder();
			sb.append("------------Warming info------------");
			sb.append(lineSeparator);
			for (String info : warnings) {
				sb.append(info);
				sb.append(lineSeparator);
			}
			sb.append("------------Warming info------------");
			LOGGER.info(sb.toString());
		}
		LOGGER.info("Read file spend time: {}ms, total time: {}ms", readFileSpendTime, totalSpendTime);
	}

	private static void cleanResource() {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (IOException e) {
			LOGGER.error("Close out put stream error: ", e);
		}
		if (warnings.size() > 0) {
			warnings.clear();
		}
	}

}
