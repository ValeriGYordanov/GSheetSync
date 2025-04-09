package com.vlr.gsheetsync.feature.sheets.data.model

import kotlinx.serialization.Serializable

object SheetSyncResponseModels {


    //region ✅ Core Spreadsheet Models
    @Serializable
    data class Spreadsheet(
        val spreadsheetId: String? = null,
        val properties: SpreadsheetProperties? = null,
        val sheets: List<Sheet>? = null,
        val namedRanges: List<NamedRange>? = null,
        val spreadsheetUrl: String? = null
    )

    /**
     * Basic properties of a spreadsheet such as title, locale, and timezone.
     */
    @Serializable
    data class SpreadsheetProperties(
        val title: String? = null,
        val locale: String? = null,
        val autoRecalc: String? = null,
        val timeZone: String? = null
    )
    //endregion

    //region ✅ Sheets and Grid Data
    /**
     * Represents a single sheet (tab) within the spreadsheet.
     */
    @Serializable
    data class Sheet(
        val properties: SheetProperties? = null,
        val data: List<GridData>? = null,
        val merges: List<GridRange>? = null,
        val conditionalFormats: List<ConditionalFormatRule>? = null,
        val protectedRanges: List<ProtectedRange>? = null
    )

    @Serializable
    data class SheetProperties(
        val sheetId: Int? = null,
        val title: String? = null,
        val index: Int? = null,
        val sheetType: String? = null,
        val gridProperties: GridProperties? = null,
        val hidden: Boolean? = null
    )

    @Serializable
    data class GridProperties(
        val rowCount: Int? = null,
        val columnCount: Int? = null,
        val frozenRowCount: Int? = null,
        val frozenColumnCount: Int? = null,
        val hideGridlines: Boolean? = null
    )
    //endregion

    //region ✅ Grid, Row, and Cell Data
    @Serializable
    data class GridData(
        val startRow: Int? = null,
        val startColumn: Int? = null,
        val rowData: List<RowData>? = null
    )

    @Serializable
    data class RowData(
        val values: List<CellData>? = null
    )

    /**
     * Represents a single cell in the grid.
     */
    @Serializable
    data class CellData(
        val userEnteredValue: ExtendedValue? = null,
        val effectiveValue: ExtendedValue? = null,
        val formattedValue: String? = null,
        val userEnteredFormat: CellFormat? = null,
        val effectiveFormat: CellFormat? = null
    )
    //endregion

    //region ✅ Cell Value and Formatting
    /**
     * Represents the value in a cell (string, number, boolean, or formula).
     */
    @Serializable
    data class ExtendedValue(
        val numberValue: Double? = null,
        val stringValue: String? = null,
        val boolValue: Boolean? = null,
        val formulaValue: String? = null
    )

    @Serializable
    data class CellFormat(
        val backgroundColor: Color? = null,
        val borders: Borders? = null,
        val padding: Padding? = null,
        val horizontalAlignment: HorizontalAlign? = null,
        val verticalAlignment: VerticalAlign? = null,
        val wrapStrategy: WrapStrategy? = null,
        val textFormat: TextFormat? = null,
        val textRotation: TextRotation? = null,
        val numberFormat: NumberFormat? = null
    )

    @Serializable
    data class TextFormat(
        val foregroundColor: Color? = null,
        val fontFamily: String? = null,
        val fontSize: Int? = null,
        val bold: Boolean? = null,
        val italic: Boolean? = null,
        val strikethrough: Boolean? = null,
        val underline: Boolean? = null
    )

    @Serializable
    data class NumberFormat(
        val type: String? = null,  // Eg: "NUMBER", "CURRENCY"
        val pattern: String? = null
    )
    //endregion

    //region ✅ Colors and Alignment
    @Serializable
    data class Color(
        val red: Float? = null,
        val green: Float? = null,
        val blue: Float? = null,
        val alpha: Float? = null
    )

    @Serializable
    enum class HorizontalAlign {
        LEFT, CENTER, RIGHT
    }

    @Serializable
    enum class VerticalAlign {
        TOP, MIDDLE, BOTTOM
    }

    @Serializable
    enum class WrapStrategy {
        OVERFLOW_CELL, LEGACY_WRAP, CLIP, WRAP
    }
    //endregion

    //region ✅ Borders, Padding, and Rotation
    @Serializable
    data class Borders(
        val top: Border? = null,
        val bottom: Border? = null,
        val left: Border? = null,
        val right: Border? = null
    )

    @Serializable
    data class Border(
        val style: String? = null, // Eg: "SOLID", "DOTTED"
        val width: Int? = null,
        val color: Color? = null
    )

    @Serializable
    data class Padding(
        val top: Int? = null,
        val bottom: Int? = null,
        val left: Int? = null,
        val right: Int? = null
    )

    @Serializable
    data class TextRotation(
        val angle: Int? = null,
        val vertical: Boolean? = null
    )
    //endregion

    //region ✅ Protected Range & Grid Range
    @Serializable
    data class ValueRange(
        val range: String,
        val majorDimension: String = "ROWS",
        val values: List<List<String>>
    )

    @Serializable
    data class BatchUpdateValuesRequest(
        val valueInputOption: String = "RAW",
        val data: List<ValueRange>
    )
    @Serializable
    data class ProtectedRange(
        val protectedRangeId: Int? = null,
        val range: GridRange? = null,
        val description: String? = null,
        val warningOnly: Boolean? = null,
        val editors: Editors? = null
    )

    @Serializable
    data class GridRange(
        val sheetId: Int? = null,
        val startRowIndex: Int? = null,
        val endRowIndex: Int? = null,
        val startColumnIndex: Int? = null,
        val endColumnIndex: Int? = null
    )

    @Serializable
    data class Editors(
        val users: List<String>? = null,
        val groups: List<String>? = null,
        val domainUsersCanEdit: Boolean? = null
    )
    //endregion

    //region Ranges
    /**
     * A named range allows referencing a range by a name.
     */
    @Serializable
    data class NamedRange(
        val namedRangeId: String? = null,
        val name: String? = null,
        val range: GridRange? = null
    )
    //endregion

    //region Rules
    @Serializable
    data class ConditionalFormatRule(
        val ranges: List<GridRange>? = null,
        val booleanRule: BooleanRule? = null,
        val gradientRule: GradientRule? = null
    )

    @Serializable
    data class BooleanRule(
        val condition: BooleanCondition? = null,
        val format: CellFormat? = null
    )

    @Serializable
    data class GradientRule(
        val minpoint: InterpolationPoint? = null,
        val midpoint: InterpolationPoint? = null,
        val maxpoint: InterpolationPoint? = null
    )

    @Serializable
    data class InterpolationPoint(
        val color: Color? = null,
        val type: String? = null,  // NUMBER, PERCENT, MIN, MAX, etc.
        val value: String? = null
    )

    @Serializable
    data class BooleanCondition(
        val type: String? = null, // TEXT_CONTAINS, DATE_BEFORE, CUSTOM_FORMULA, etc.
        val values: List<ConditionValue>? = null
    )

    @Serializable
    data class ConditionValue(
        val userEnteredValue: String? = null
    )
    //endregion

    //region ✅ BatchUpdateSpreadsheetRequest and Response
    /**
     * Root request for batch spreadsheet updates.
     */
    @Serializable
    data class BatchUpdateSpreadsheetRequest(
        val requests: List<Request>,
        val includeSpreadsheetInResponse: Boolean? = null,
        val responseRanges: List<String>? = null,
        val responseIncludeGridData: Boolean? = null
    )

    @Serializable
    data class BatchUpdateSpreadsheetResponse(
        val spreadsheetId: String? = null,
        val replies: List<Response>? = null,
        val updatedSpreadsheet: Spreadsheet? = null
    )
    //endregion

    //region ✅ Request Types (Partial List — Expandable)
    @Serializable
    data class Request(
        val addSheet: AddSheetRequest? = null,
        val deleteSheet: DeleteSheetRequest? = null,
        val updateSheetProperties: UpdateSheetPropertiesRequest? = null,
        val appendCells: AppendCellsRequest? = null,
        val repeatCell: RepeatCellRequest? = null,
        val updateCells: UpdateCellsRequest? = null,
        val mergeCells: MergeCellsRequest? = null
    )

    @Serializable
    data class AddSheetRequest(val properties: SheetProperties? = null)

    @Serializable
    data class DeleteSheetRequest(val sheetId: Int)

    @Serializable
    data class UpdateSheetPropertiesRequest(
        val properties: SheetProperties,
        val fields: String
    )

    @Serializable
    data class AppendCellsRequest(
        val sheetId: Int,
        val rows: List<RowData>,
        val fields: String
    )

    @Serializable
    data class RepeatCellRequest(
        val range: GridRange,
        val cell: CellData,
        val fields: String
    )

    @Serializable
    data class UpdateCellsRequest(
        val rows: List<RowData>,
        val fields: String,
        val range: GridRange? = null,
        val start: GridCoordinate? = null
    )

    @Serializable
    data class MergeCellsRequest(
        val range: GridRange,
        val mergeType: String // MERGE_ALL, MERGE_COLUMNS, MERGE_ROWS
    )
    //endregion

    //region ✅ Response Object (Partial)
    @Serializable
    data class Response(
        val addSheet: AddSheetResponse? = null,
        val updateCells: UpdateCellsResponse? = null,
        val appendCells: AppendCellsResponse? = null
    )

    @Serializable
    data class AddSheetResponse(val properties: SheetProperties? = null)

    @Serializable
    data class UpdateCellsResponse(val updatedCells: Int? = null)

    @Serializable
    data class AppendCellsResponse(val updatedRange: String? = null)
    //endregion

    //region ✅ Other Supporting Types
    @Serializable
    data class GridCoordinate(
        val sheetId: Int,
        val rowIndex: Int,
        val columnIndex: Int
    )
    //endregion

}
