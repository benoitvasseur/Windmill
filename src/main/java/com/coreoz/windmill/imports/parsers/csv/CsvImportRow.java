package com.coreoz.windmill.imports.parsers.csv;

import java.util.Iterator;

import com.coreoz.windmill.imports.FileSchema;
import com.coreoz.windmill.imports.ImportCell;
import com.coreoz.windmill.imports.ImportRow;

public class CsvImportRow implements ImportRow {

	private final int currentRowIndex;
	private final FileSchema fileSchema;
	private final String[] row;

	public CsvImportRow(int currentRowIndex, FileSchema fileSchema, String[] row) {
		this.currentRowIndex = currentRowIndex;
		this.fileSchema = fileSchema;
		this.row = row;
	}

	@Override
	public Iterator<ImportCell> iterator() {
		return new CsvCellIterator(row);
	}

	@Override
	public int rowIndex() {
		return currentRowIndex;
	}

	@Override
	public ImportCell cell(String columnName) {
		return cell(fileSchema.columnIndex(columnName));
	}

	@Override
	public ImportCell cell(Integer columnIndex) {
		return new CsvImportCell(columnIndex, row[columnIndex]);
	}

}