package com.github.khangnt.mcp.ui

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.github.khangnt.mcp.R
import kotlinx.android.synthetic.main.activity_feed_back.*

class FeedbackActivity : AppCompatActivity() {

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_back)

        supportActionBar?.title = "In-app Feedback"

        btnFRequest.setOnClickListener{
            btnFRequest.isSelected = true
            btnBReport.isSelected = false
            btnQuestion.isSelected = false
        }

        btnBReport.setOnClickListener{
            btnFRequest.isSelected = false
            btnBReport.isSelected = true
            btnQuestion.isSelected = false
        }

        btnQuestion.setOnClickListener{
            btnFRequest.isSelected = false
            btnBReport.isSelected = false
            btnQuestion.isSelected = true
        }
    }
}
