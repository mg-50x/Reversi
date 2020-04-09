package com.example.reversi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_menu.*


class Menu : AppCompatActivity() {
    public val ruleReversi = 0
    public val ruleOthello = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        btn_start.setOnClickListener {
            var prmStone :Int = Color.BLACK
            var prmRule =  ruleReversi
            if(tBtn_stone.isChecked) prmStone = Color.WHITE
            if(tBtn_rule.isChecked) prmRule = ruleOthello

            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("stone", prmStone)
            intent.putExtra("rule", prmRule)
            startActivity(intent)
        }

        btn_end.setOnClickListener(){
            finish()
        }
    }
}
