package com.coreoz.windmill.importer.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.coreoz.windmill.Row;
import com.coreoz.windmill.importer.FileParser;
import com.coreoz.windmill.importer.FileSchema;
import com.coreoz.windmill.importer.FileSource;

import lombok.SneakyThrows;

public abstract class BaseExcelParser implements FileParser {

	private final String sheetName;
	private final Integer sheetIndex;

	public BaseExcelParser() {
		// by default, use the first sheet
		this(null, 0);
	}

	public BaseExcelParser(String sheetName) {
		this(sheetName, null);
	}

	public BaseExcelParser(Integer sheetIndex) {
		this(null, sheetIndex);
	}

	private BaseExcelParser(String sheetName, Integer sheetIndex) {
		if(sheetName == null && sheetIndex == null) {
			throw new IllegalArgumentException("Either the sheetName or the sheetIndex must be specified");
		}

		this.sheetName = sheetName;
		this.sheetIndex = sheetIndex;
	}

	protected abstract Workbook openWorkbook(InputStream sourceInputStream) throws IOException;

	/**
	 * @throws IOException
	 */
	@SneakyThrows
	@Override
	public Stream<Row> parse(FileSource source) {
		Workbook workbook = openWorkbook(source.toInputStream());
		Sheet sheet = selectSheet(workbook);

		return stream(new ExcelRowIterator(sheet.rowIterator()))
			.onClose(() -> close(workbook));
	}

	@SneakyThrows
	public static void close(AutoCloseable closeable) {
		closeable.close();
	}

	private Sheet selectSheet(Workbook workbook) {
		if(sheetIndex != null) {
			return workbook.getSheetAt(sheetIndex);
		}
		return workbook.getSheet(sheetName);
	}

	/**
	 * Returns a sequential {@link Stream} of the remaining contents of
	 * {@code iterator}. Do not use {@code iterator} directly after passing it to
	 * this method.
	 */
	private static <T> Stream<T> stream(Iterator<T> iterator) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
	}

	private static class ExcelRowIterator implements Iterator<Row> {

		private final Iterator<org.apache.poi.ss.usermodel.Row> rowIterator;
		private FileSchema fileSchema;

		public ExcelRowIterator(Iterator<org.apache.poi.ss.usermodel.Row> rowIterator) {
			this.rowIterator = rowIterator;
			this.fileSchema = null;
		}

		@Override
		public boolean hasNext() {
			return rowIterator.hasNext();
		}

		@Override
		public Row next() {
			org.apache.poi.ss.usermodel.Row nextExcelRow = rowIterator.next();

			if (fileSchema == null) {
				fileSchema = new FileSchema(
					StreamSupport
						.stream(Spliterators.spliteratorUnknownSize(ExcelRow.cellIterator(nextExcelRow), 0), false)
						.collect(Collectors.toList())
				);
			}

			return new ExcelRow(nextExcelRow, fileSchema);
		}

	}

}
