package com.example.animeplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.animeplayer.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val seriesList = mutableListOf<Series>()
    private lateinit var adapter: SeriesAdapter

    private val folderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            scanDirectory(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SeriesAdapter(seriesList) { series ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putParcelableArrayListExtra("EPISODES", ArrayList(series.seasons[0].episodes))
                putExtra("START_INDEX", 0)
            }
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter
        binding.btnSelectFolder.setOnClickListener { folderPicker.launch(null) }
    }

    private fun scanDirectory(treeUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val root = DocumentFile.fromTreeUri(this@MainActivity, treeUri) ?: return@launch
            val tempList = mutableListOf<Series>()

            root.listFiles().filter { it.isDirectory }.forEach { seriesDir ->
                val seasons = mutableListOf<Season>()
                seriesDir.listFiles().filter { it.isDirectory }.sortedBy { it.name }.forEach { seasonDir ->
                    val episodes = seasonDir.listFiles()
                        .filter { it.name?.endsWith(".mkv") == true || it.name?.endsWith(".mp4") == true }
                        .sortedBy { it.name }
                        .mapIndexed { index, file ->
                            Episode(file.uri.toString(), file.name ?: "", seriesDir.name ?: "", index)
                        }
                    if (episodes.isNotEmpty()) seasons.add(Season(seasonDir.name ?: "", episodes))
                }
                if (seasons.isNotEmpty()) tempList.add(Series(seriesDir.name ?: "", seasons))
            }

            withContext(Dispatchers.Main) {
                seriesList.clear()
                seriesList.addAll(tempList)
                adapter.notifyDataSetChanged()
            }
        }
    }
}