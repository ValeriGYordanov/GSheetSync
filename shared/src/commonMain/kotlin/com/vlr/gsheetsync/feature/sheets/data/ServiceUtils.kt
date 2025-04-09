package com.vlr.gsheetsync.feature.sheets.data

/* Extension Functions */

/**
 * Maximum allowed column number (ZZ = 702)
 */
private const val MAX_COLUMN_NUMBER = 702

/**
 * Maximum allowed row number
 */
private const val MAX_ROW_NUMBER = 100000

/**
 * Validates if a string is a proper cell reference (e.g., "A1", "ZZ100")
 */
internal fun String.isValidCellReference(): Boolean {
    if (isEmpty()) return false
    val letters = takeWhile { it.isLetter() }
    val numbers = dropWhile { it.isLetter() }
    return letters.isNotEmpty() &&
            numbers.isNotEmpty() &&
            numbers.all { it.isDigit() } &&
            letters.toColumnNumber() <= MAX_COLUMN_NUMBER &&
            numbers.toInt() <= MAX_ROW_NUMBER
}

/**
 * Parses sheet range with validation.
 * @throws IllegalArgumentException if range format is invalid
 */
internal fun String.parseSheetRange(): Pair<Int, Int> {
    require(contains('!')) { "Sheet range must contain '!' separator" }
    val cellRef = substringAfterLast('!').substringBefore(':')
    require(cellRef.isValidCellReference()) { "Invalid cell reference in range: $this" }
    return cellRef.parseCellReference()
}

/**
 * Parses cell reference with validation.
 * @throws IllegalArgumentException if reference is invalid
 */
internal fun String.parseCellReference(): Pair<Int, Int> {
    require(isValidCellReference()) { "Invalid cell reference: $this" }
    val col = takeWhile { it.isLetter() }
    val row = dropWhile { it.isLetter() }
    return col.toColumnNumber() to row.toInt().coerceIn(1, MAX_ROW_NUMBER)
}

/**
 * Converts column number to name with bounds checking.
 * Returns "A" for invalid inputs.
 */
internal fun Int.toColumnName(): String = when {
    this <= 0 -> "A"
    this > MAX_COLUMN_NUMBER -> "A"
    else -> buildString {
        var n = this@toColumnName
        while (n > 0) {
            n--
            insert(0, 'A' + (n % 26))
            n /= 26
        }
    }
}

/**
 * Converts column name to number with validation.
 * Returns 1 (for "A") for invalid inputs.
 */
internal fun String.toColumnNumber(): Int = when {
    isEmpty() -> 1
    any { !it.isLetter() } -> 1
    else -> uppercase().fold(0) { acc, c ->
        (acc * 26 + (c - 'A' + 1)).coerceAtMost(MAX_COLUMN_NUMBER)
    }.coerceAtLeast(1)
}