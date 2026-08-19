package com.chlqudco.seoulcrowdinglevelmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chlqudco.seoulcrowdinglevelmap.data.CrowdRepository
import com.chlqudco.seoulcrowdinglevelmap.ui.CrowdRadarApp
import com.chlqudco.seoulcrowdinglevelmap.ui.CrowdViewModel
import com.chlqudco.seoulcrowdinglevelmap.ui.CrowdViewModelFactory
import com.chlqudco.seoulcrowdinglevelmap.ui.theme.SeoulCrowdingLevelMapTheme

class MainActivity : ComponentActivity() {
    private val repository by lazy { CrowdRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeoulCrowdingLevelMapTheme {
                val factory = remember { CrowdViewModelFactory(repository) }
                val crowdViewModel: CrowdViewModel = viewModel(factory = factory)
                CrowdRadarApp(crowdViewModel)
            }
        }
    }
}
