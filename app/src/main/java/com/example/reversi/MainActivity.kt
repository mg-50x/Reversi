package com.example.reversi

import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.reversi.board.BoardCell
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity() : AppCompatActivity() {
    val ruleReversi = 0
    val ruleOthello = 1
    var rule: Int = 0

    private val playerViewFormat = "%s のターン"
    private val gameEndFormat = "%s の勝利"
    private val black = "黒"
    private val white = "白"

    private var playerColor: Int = Color.BLACK
    private var initReversiCount = 0
    private val initCells: ArrayList<BoardCell> = ArrayList()
    var turnCount = 64

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerColor = intent.getIntExtra("stone", Color.BLACK)
        rule = intent.getIntExtra("rule", 0)

        var size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,40f, this.getResources().displayMetrics)

        btn_back.setOnClickListener(){
            finish()
        }

        for(i: Int in 0..7){
            var row = TableRow(this)
            row.layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT)
            row.id = i

            for(x: Int in 0..7){
                var cell = BoardCell(this)
                cell.id = x

                if(rule == ruleOthello){
                    if((i == 3 && x == 3) || (i == 4 && x == 4)) cell.setColor(Color.WHITE)
                    if((i == 3 && x == 4) || (i == 4 && x == 3)) cell.setColor(Color.BLACK)
                    turnCount = 60
                    cell.setOnClickListener(CellSetOnClickListener)
                }
                else {
                    if((i == 3 && x == 3) || (i == 4 && x == 4) || (i == 3 && x == 4) || (i == 4 && x == 3)) {
                        initCells.add(cell)

                        cell.setOnClickListener {
                            if(initReversiCellClick(it as BoardCell)) initReversiCount++
                            gameSideChange(false)

                            initCells.shuffle()
                            if(initReversiCellClick(initCells.first { x -> x.drawColor == Color.TRANSPARENT })) initReversiCount++
                            gameSideChange(false)

                            if(initReversiCount > 3){
                                setCellListener()
                            }
                        }
                    }
                }

                var lp = TableRow.LayoutParams(size.toInt(), size.toInt())
                lp.setMargins(2,2,2,2)
                cell.layoutParams = lp
                row.addView(cell)
            }
            Board.addView(row)
        }
        switchPlayerViewText()
    }

    fun setCellListener(){
        for(rowIndex: Int in 0..7){
            var row = Board.getChildAt(rowIndex) as TableRow

            for(cellIndex: Int in 0..7){
                var cell = (row.getChildAt(cellIndex) as BoardCell)

                cell.setOnClickListener(CellSetOnClickListener)
            }
        }
    }

    val CellSetOnClickListener = { view:View ->
        if(cellClick(view as BoardCell) && gameSideChange()){
            Handler().postDelayed(Runnable {
                var playerPass = false
                do{
                    var cpuPass = !processingCPU()
                    if(!gameSideChange(cpuPass)) break

                    var trueCount = searchTrueCells().count()
                    if(trueCount < 1){
                        if(gameSideChange(false)) playerPass = true
                        if(cpuPass && playerPass){
                            gameEnd()
                            break
                        }else{
                            Log.d("Player","PASS")
                            Toast.makeText(this, "Player: パス", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else playerPass = false
                }while(playerPass && turnCount > 0)
            }, 500)
        }
    }

    fun switchPlayerViewText(){
        if(playerColor == Color.BLACK) Player.text = playerViewFormat.format(black)
        else Player.text = playerViewFormat.format(white)
    }

    fun gameEnd(){
        var winner : String = ""
        var blackCount = 0
        var whiteCount = 0

        for(rowIndex: Int in 0..7){
            var row = Board.getChildAt(rowIndex) as TableRow

            for(cellIndex: Int in 0..7){
                var cell = (row.getChildAt(cellIndex) as BoardCell)

                if(cell.drawColor == Color.BLACK) blackCount++
                else whiteCount++
            }
        }

        if(blackCount > whiteCount) winner = black
        else winner = white

        Player.text = gameEndFormat.format(winner)

        Board.isEnabled = false
        btn_back.visibility = View.VISIBLE
    }

    fun searchTrueCells(): ArrayList<HitLocation>{
        if(playerColor == Color.BLACK) Log.d("searchTrueCells  Player", black)
        else Log.d("searchTrueCells  Player", white)

        var aryLocation: ArrayList<HitLocation> = ArrayList()
        for(rowIndex: Int in 0..7){
            var targetRow = Board.getChildAt(rowIndex) as TableRow
            for(cellIndex: Int in 0..7){
                var targetCell = targetRow.getChildAt(cellIndex) as BoardCell
                if(targetCell.drawColor == Color.TRANSPARENT){
                    var top: HitLocation? = null
                    var left:  HitLocation? = null
                    var right:  HitLocation? = null
                    var bottom:  HitLocation? = null
                    var rightTop:  HitLocation? = null
                    var rightBottom:  HitLocation? = null
                    var leftTop:  HitLocation? = null
                    var leftBottom:  HitLocation? = null

                    if(rowIndex > 0) top = findTop(rowIndex, cellIndex)
                    if(cellIndex > 0) left = findLeft(rowIndex, cellIndex)
                    if(cellIndex < 7) right = findRight(rowIndex, cellIndex)
                    if(rowIndex < 7) bottom = findBottom(rowIndex, cellIndex)
                    if(rowIndex > 0 && cellIndex < 7) rightTop = findRightTop(rowIndex, cellIndex)
                    if(rowIndex < 7 && cellIndex < 7) rightBottom = findRightBottm(rowIndex, cellIndex)
                    if(rowIndex > 0 && cellIndex > 0) leftTop = findLeftTop(rowIndex, cellIndex)
                    if(rowIndex < 7 && cellIndex > 0) leftBottom = findLeftBottm(rowIndex, cellIndex)

                    if(top != null && top.hitCellIndex != -1) aryLocation.add(top)
                    if(left != null && left.hitCellIndex != -1) aryLocation.add(left)
                    if(right != null && right.hitCellIndex != -1) aryLocation.add(right)
                    if(bottom != null && bottom.hitCellIndex != -1) aryLocation.add(bottom)
                    if(rightTop != null && rightTop.hitCellIndex != -1) aryLocation.add(rightTop)
                    if(rightBottom != null && rightBottom.hitCellIndex != -1) aryLocation.add(rightBottom)
                    if(leftTop != null && leftTop.hitCellIndex != -1) aryLocation.add(leftTop)
                    if(leftBottom != null && leftBottom.hitCellIndex != -1) aryLocation.add(leftBottom)
                }
            }
        }

        return aryLocation
    }

    fun processingCPU(): Boolean{
        var result = false
        var aryLocation: ArrayList<HitLocation> = searchTrueCells()

        if(aryLocation.count() > 0){
            var target = aryLocation.shuffled().first()
            var targetRow = Board.getChildAt(target.rowIndex) as TableRow
            var targetCell = targetRow.getChildAt(target.cellIndex) as BoardCell
            Log.d("CPU", target.rowIndex.toString() + "," + target.cellIndex.toString() + " SET")

            cellClick(targetCell)
            result = true
        }else if(turnCount > 0){
            Log.d("CPUState", "PASS")
            Toast.makeText(this, "CPU: パス", Toast.LENGTH_SHORT).show()
        }

        return result
    }

    fun cellClick(cell :BoardCell): Boolean{
        var result = false
        if(cell.drawColor == Color.TRANSPARENT){
            var row = (cell.parent as TableRow)
            var rowIndex = Board.indexOfChild(row)
            var cellIndex = row.indexOfChild(cell)
            Log.d("cellClick","rowIndex: " + cellIndex.toString() + ", cellIndex" + cellIndex.toString())

            var top = false
            if(rowIndex > 0) top = reverseTop(findTop(rowIndex, cellIndex))
            var left = false
            if(cellIndex > 0) left = reverseLeft(findLeft(rowIndex, cellIndex))
            var right = false
            if(cellIndex < 7) right = reverseRight(findRight(rowIndex, cellIndex))
            var bottom = false
            if(rowIndex < 7) bottom = reverseBottom(findBottom(rowIndex, cellIndex))
            var rightTop = false
            if(rowIndex > 0 && cellIndex < 7) rightTop = reverseRightTop(findRightTop(rowIndex, cellIndex))
            var rightBottom = false
            if(rowIndex < 7 && cellIndex < 7) rightBottom = reverseRightBottm(findRightBottm(rowIndex, cellIndex))
            var leftTop = false
            if(rowIndex > 0 && cellIndex > 0) leftTop = reverseLeftTop(findLeftTop(rowIndex, cellIndex))
            var leftBottom = false
            if(rowIndex < 7 && cellIndex > 0) leftBottom = reverseLeftBottm(findLeftBottm(rowIndex, cellIndex))

            if(top || left || right || bottom || rightTop || rightBottom|| leftTop || leftBottom) {
                cell.changeColor(playerColor)
                result = true
            }
        }
        return result
    }

    fun initReversiCellClick(cell :BoardCell): Boolean{
        var result = false
        if(cell.drawColor == Color.TRANSPARENT) {
            cell.changeColor(playerColor)
            result = true
        }
        return result
    }

    fun gameSideChange(turn: Boolean = true): Boolean{
        var result = true
        if(playerColor == Color.BLACK){
            playerColor = Color.WHITE
            Log.d("Player", white)
        }
        else {
            playerColor = Color.BLACK
            Log.d("Player", black)
        }
        switchPlayerViewText()

        if(turn){
            turnCount--
            if(turnCount == 0){
                gameEnd()
                result = false
            }
            Log.d("GameCount: ", turnCount.toString())
        }

        return result
    }

    fun findLeft(rowIndex: Int, cellIndex: Int): HitLocation{
        var result = -1
        var row = Board.getChildAt(rowIndex) as TableRow
        var firstCell = (row.getChildAt(cellIndex - 1) as BoardCell)

        if(firstCell.drawColor != playerColor && firstCell.drawColor != Color.TRANSPARENT ){
            for(ci: Int in (cellIndex - 2) downTo 0){
                Log.d("reverseLeft","ci: " + ci.toString())
                var cell = row.getChildAt(ci) as BoardCell

                if(cell.drawColor == Color.TRANSPARENT) break
                else if(cell.drawColor == playerColor){
                    result = ci
                    break
                }
            }
        }
        return HitLocation(rowIndex, cellIndex, rowIndex, result, Direction().left)
    }

    fun reverseLeft(hitLocation: HitLocation) : Boolean {
        var result = false
        var row = Board.getChildAt(hitLocation.rowIndex) as TableRow
        var hitCellIndex = hitLocation.hitCellIndex

        if(hitCellIndex != -1){
            for(ci: Int in (hitCellIndex + 1)..(hitLocation.cellIndex - 1)){
                var cell = row.getChildAt(ci) as BoardCell
                if(cell.drawColor != playerColor){
                    cell.changeColor(playerColor)
                }
            }
            result = true
        }
        return result
    }

    fun findRight(rowIndex: Int, cellIndex: Int): HitLocation{
        var result = -1
        var row = Board.getChildAt(rowIndex) as TableRow
        var firstCell = (row.getChildAt(cellIndex + 1) as BoardCell)

        if(firstCell.drawColor != playerColor && firstCell.drawColor != Color.TRANSPARENT){
            for(ci: Int in (cellIndex + 2)..7){
                Log.d("reverseRight","ci: " + ci.toString())
                var cell = row.getChildAt(ci) as BoardCell

                if(cell.drawColor == Color.TRANSPARENT) break
                else if(cell.drawColor == playerColor){
                    result = ci
                    break
                }
            }
        }
        return HitLocation(rowIndex, cellIndex, rowIndex, result, Direction().right)
    }

    fun reverseRight(hitLocation: HitLocation) : Boolean {
        var result = false
        var row = Board.getChildAt(hitLocation.rowIndex) as TableRow
        var hitCellIndex = hitLocation.hitCellIndex

        if(hitCellIndex != -1){
            for(ci: Int in (hitCellIndex - 1) downTo (hitLocation.cellIndex + 1)){
                var cell = row.getChildAt(ci) as BoardCell
                if(cell.drawColor != playerColor){
                    cell.changeColor(playerColor)
                }
            }
            result = true
        }

        return result
    }

    fun findTop(rowIndex: Int, cellIndex: Int): HitLocation{
        var result = -1
        var firstRow = Board.getChildAt(rowIndex - 1) as TableRow
        var firstCell = firstRow.getChildAt(cellIndex) as BoardCell

        if(firstCell.drawColor != playerColor && firstCell.drawColor != Color.TRANSPARENT) {
            for (ri: Int in (rowIndex - 2) downTo 0) {
                Log.d("reverseTop","ci: " + ri.toString())
                var row = Board.getChildAt(ri) as TableRow
                var cell = row.getChildAt(cellIndex) as BoardCell

                if (cell.drawColor == Color.TRANSPARENT) break
                else if (cell.drawColor == playerColor) {
                    result = ri
                    break
                }
            }
        }

        return HitLocation(rowIndex, cellIndex, rowIndex, result, Direction().top)
    }

    fun reverseTop(hitLocation: HitLocation) : Boolean {
        var result = false
        var hitCellIndex = hitLocation.hitCellIndex

        if (hitCellIndex != -1) {
            for (ri: Int in (hitCellIndex + 1)..(hitLocation.rowIndex - 1)) {
                var row = Board.getChildAt(ri) as TableRow
                var cell = row.getChildAt(hitLocation.cellIndex) as BoardCell
                if (cell.drawColor != playerColor) {
                    cell.changeColor(playerColor)
                }
            }
            result = true
        }

        return result
    }

    fun findBottom(rowIndex: Int, cellIndex: Int) : HitLocation {
        var result = -1
        var firstRow = Board.getChildAt(rowIndex + 1) as TableRow
        var firstCell = firstRow.getChildAt(cellIndex) as BoardCell

        if(firstCell.drawColor != playerColor && firstCell.drawColor != Color.TRANSPARENT) {
            for (ri: Int in (rowIndex + 2)..7) {
                Log.d("reverseBottom","ci: " + ri.toString())
                var row = Board.getChildAt(ri) as TableRow
                var cell = row.getChildAt(cellIndex) as BoardCell

                if (cell.drawColor == Color.TRANSPARENT) break
                else if (cell.drawColor == playerColor) {
                    result = ri
                    break
                }
            }
        }

        return HitLocation(rowIndex, cellIndex, rowIndex, result, Direction().bottom)
    }

    fun reverseBottom(hitLocation: HitLocation) : Boolean {
        var result = false
        var hitCellIndex = hitLocation.hitCellIndex

        if (hitCellIndex != -1) {
            for (ri: Int in (hitCellIndex - 1) downTo (hitLocation.rowIndex + 1)) {
                var row = Board.getChildAt(ri) as TableRow
                var cell = row.getChildAt(hitLocation.cellIndex) as BoardCell
                if (cell.drawColor != playerColor) {
                    cell.changeColor(playerColor)
                }
            }
            result = true
        }

        return result
    }

    fun findRightTop(rowIndex: Int, cellIndex: Int) : HitLocation {
        var firstRow = Board.getChildAt(rowIndex - 1) as TableRow
        var firstCell = firstRow.getChildAt(cellIndex + 1) as BoardCell
        var hitCellIndex = -1
        var hitRowIndex = -1
        var i = cellIndex + 2

        if(firstCell.drawColor != playerColor && firstCell.drawColor != Color.TRANSPARENT) {
            for (ri: Int in (rowIndex - 2) downTo 0) {
                Log.d("reverseRightTop","ri: " + ri.toString() + ", i: " + i.toString())
                if(i < 8){
                    var row = Board.getChildAt(ri) as TableRow
                    var cell = row.getChildAt(i) as BoardCell

                    if (cell.drawColor == Color.TRANSPARENT) break
                    else if (cell.drawColor == playerColor) {
                        hitRowIndex = ri
                        hitCellIndex = i
                        break
                    }
                }
                i++
            }
        }

        return HitLocation(rowIndex, cellIndex, hitRowIndex, hitCellIndex, Direction().rightTop)
    }

    fun reverseRightTop(hitLocation: HitLocation) : Boolean {
        var result = false

        var hitCellIndex = hitLocation.hitCellIndex
        var hitRowIndex = hitLocation.hitRowIndex

        if (hitCellIndex != -1 && hitRowIndex != -1) {
            var i = hitCellIndex - 1
            for (ri: Int in (hitRowIndex + 1)..(hitLocation.rowIndex - 1)) {
                var row = Board.getChildAt(ri) as TableRow
                var cell = row.getChildAt(i) as BoardCell
                if (cell.drawColor != playerColor) {
                    cell.changeColor(playerColor)
                }
                i--
            }
            result = true
        }

        return result
    }

    fun findRightBottm(rowIndex: Int, cellIndex: Int) : HitLocation{
        var firstRow = Board.getChildAt(rowIndex + 1) as TableRow
        var firstCell = firstRow.getChildAt(cellIndex + 1) as BoardCell
        var hitCellIndex = -1
        var hitRowIndex = -1
        var i = cellIndex + 2

        if(firstCell.drawColor != playerColor && firstCell.drawColor != Color.TRANSPARENT) {
            for (ri: Int in (rowIndex + 2)..7) {
                Log.d("reverseRightBottm","ri: " + ri.toString() + ", i: " + i.toString())
                if(i < 8) {
                    var row = Board.getChildAt(ri) as TableRow
                    var cell = row.getChildAt(i) as BoardCell

                    if (cell.drawColor == Color.TRANSPARENT) break
                    else if (cell.drawColor == playerColor) {
                        hitRowIndex = ri
                        hitCellIndex = i
                        break
                    }
                }
                i++
            }
        }

        return HitLocation(rowIndex, cellIndex, hitRowIndex, hitCellIndex, Direction().rightBottom)
    }

    fun reverseRightBottm(hitLocation: HitLocation) : Boolean {
        var result = false
        var hitCellIndex = hitLocation.hitCellIndex
        var hitRowIndex = hitLocation.hitRowIndex

        if (hitCellIndex != -1 && hitRowIndex != -1) {
            var i = hitCellIndex - 1
            for (ri: Int in (hitRowIndex - 1) downTo (hitLocation.rowIndex + 1)) {
                var row = Board.getChildAt(ri) as TableRow
                var cell = row.getChildAt(i) as BoardCell
                if (cell.drawColor != playerColor) {
                    cell.changeColor(playerColor)
                }
                i--
            }
            result = true
        }

        return result
    }

    fun findLeftTop(rowIndex: Int, cellIndex: Int) : HitLocation{
        var firstRow = Board.getChildAt(rowIndex - 1) as TableRow
        var firstCell = firstRow.getChildAt(cellIndex - 1) as BoardCell
        var hitCellIndex = -1
        var hitRowIndex = -1
        var i = cellIndex - 2

        if(firstCell.drawColor != playerColor && firstCell.drawColor != Color.TRANSPARENT) {
            for (ri: Int in (rowIndex - 2) downTo 0) {
                Log.d("reverseLeftTop","ri: " + ri.toString() + ", i: " + i.toString())
                if(i > -1){
                    var row = Board.getChildAt(ri) as TableRow
                    var cell = row.getChildAt(i) as BoardCell

                    if (cell.drawColor == Color.TRANSPARENT) break
                    else if (cell.drawColor == playerColor) {
                        hitRowIndex = ri
                        hitCellIndex = i
                        break
                    }
                }
                i--
            }
        }

        return HitLocation(rowIndex, cellIndex, hitRowIndex, hitCellIndex, Direction().leftTop)
    }

    fun reverseLeftTop(hitLocation: HitLocation) : Boolean {
        var result = false
        var hitCellIndex = hitLocation.hitCellIndex
        var hitRowIndex = hitLocation.hitRowIndex

        if (hitCellIndex != -1 && hitRowIndex != -1) {
            var i = hitCellIndex + 1
            for (ri: Int in (hitRowIndex + 1)..(hitLocation.rowIndex - 1)) {
                var row = Board.getChildAt(ri) as TableRow
                var cell = row.getChildAt(i) as BoardCell
                if (cell.drawColor != playerColor) {
                    cell.changeColor(playerColor)
                }
                i++
            }
            result = true
        }

        return result
    }

    fun findLeftBottm(rowIndex: Int, cellIndex: Int) : HitLocation{
        var firstRow = Board.getChildAt(rowIndex + 1) as TableRow
        var firstCell = firstRow.getChildAt(cellIndex - 1) as BoardCell
        var hitCellIndex = -1
        var hitRowIndex = -1
        var i = cellIndex - 2

        if(firstCell.drawColor != playerColor && firstCell.drawColor != Color.TRANSPARENT) {
            for (ri: Int in (rowIndex + 2)..7) {
                Log.d("reverseLeftBottm","ri: " + ri.toString() + ", i: " + i.toString())
                if(i > -1) {
                    var row = Board.getChildAt(ri) as TableRow
                    var cell = row.getChildAt(i) as BoardCell

                    if (cell.drawColor == Color.TRANSPARENT) break
                    else if (cell.drawColor == playerColor) {
                        hitRowIndex = ri
                        hitCellIndex = i
                        break
                    }
                }
                i--
            }
        }

        return HitLocation(rowIndex, cellIndex, hitRowIndex, hitCellIndex, Direction().leftBottom)
    }

    fun reverseLeftBottm(hitLocation: HitLocation) : Boolean {
        var result = false
        var hitCellIndex = hitLocation.hitCellIndex
        var hitRowIndex = hitLocation.hitRowIndex

        if (hitCellIndex != -1 && hitRowIndex != -1) {
            var i = hitCellIndex + 1
            for (ri: Int in (hitRowIndex - 1) downTo (hitLocation.rowIndex + 1)) {
                var row = Board.getChildAt(ri) as TableRow
                var cell = row.getChildAt(i) as BoardCell
                if (cell.drawColor != playerColor) {
                    cell.changeColor(playerColor)
                }
                i++
            }
            result = true
        }

        return result
    }

    class HitLocation(var rowIndex: Int, var cellIndex: Int, var hitRowIndex: Int, var hitCellIndex: Int, var direction: Int){
    }

    class Direction{
        val top = 0
        val left = 1
        val right = 2
        val bottom = 3
        val rightTop = 4
        val rightBottom = 5
        val leftTop = 6
        val leftBottom = 7
    }
}
