package com.yywspace.simplefilemanager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.yywspace.simplefilemanager.adapters.FileListAdapter
import com.yywspace.simplefilemanager.viewmodels.FileListViewModel
import kotlinx.android.synthetic.main.dialog_file_list_sort_layout.view.*
import kotlinx.android.synthetic.main.fragment_file_list.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


private const val ARG_FOLDER_PATH = "folder_path"

class FileListFragment : Fragment() {
    private val viewModel: FileListViewModel by viewModels()
    private var folderPath: String? = null
    private lateinit var adapter: FileListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            folderPath = it.getString(ARG_FOLDER_PATH)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_list, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.file_list_page_label)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FileListAdapter().apply {
            onItemClickListener = {
                Toast.makeText(requireContext(), it.toFile().extension, Toast.LENGTH_SHORT).show()
                if (Files.isDirectory(it))
                    (parentFragment as FileListContainerFragment)
                        .addCrumbItem(
                            it.fileName.toString(),
                            newInstance(it.toFile().absolutePath)
                        )
            }
        }

        fileRecyclerView.apply {
            adapter = this@FileListFragment.adapter
            layoutManager = LinearLayoutManager(requireActivity())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }
        viewModel.initData(Paths.get(folderPath!!))
        viewModel.getLiveData().observe(viewLifecycleOwner, Observer { pathList ->
            adapter.submitList(pathList)
            fileRecyclerView.apply {
                postDelayed({
                    (layoutManager as LinearLayoutManager)
                        .scrollToPositionWithOffset(0, 0)
                }, 100)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_file_list, menu)
        menu.findItem(R.id.hidden_file).isChecked = viewModel.isHasHiddenFile()
    }

    @SuppressLint("InflateParams")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val sortType = viewModel.getCurrentSortType()
        val sortOrder = viewModel.getCurrentSortOrder()

        return when (item.itemId) {
            R.id.sort -> {
                with(
                    LayoutInflater.from(requireContext())
                        .inflate(R.layout.dialog_file_list_sort_layout, null, false)
                ) {
                    val view = this
                    sortReverseOrder.isChecked = sortOrder
                    when (sortType) {
                        "name" -> sortName.isChecked = true
                        "size" -> sortSize.isChecked = true
                        "time" -> sortTime.isChecked = true
                    }
                    AlertDialog.Builder(requireContext()).apply {
                        setTitle("排序")
                        setView(view)
                        setPositiveButton("确认") { _, _ ->
                            when {
                                sortName.isChecked ->
                                    viewModel.sortByName(sortReverseOrder.isChecked)
                                sortSize.isChecked ->
                                    viewModel.sortBySize(sortReverseOrder.isChecked)
                                sortTime.isChecked ->
                                    viewModel.sortByModifyTime(sortReverseOrder.isChecked)
                            }
                        }
                        show()
                    }
                }
                true
            }
            R.id.hidden_file -> {
                item.isChecked = !item.isChecked
                viewModel.changeHiddenFileStatus(item.isChecked)
                true
            }
            R.id.search -> {
                val bundle = Bundle()
                bundle.putString(CURRENT_PATH, folderPath)
                NavHostFragment.findNavController(this)
                    .navigate(
                        R.id.action_fileListContainerFragment_to_fileSearchFragment,
                        bundle
                    )
                true
            }
            R.id.create_new_folder -> {
                val view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_create_folder_layout, null, false)
                val editText = view.findViewById<EditText>(R.id.folder_create_edit_text)
                val dialog = AlertDialog.Builder(requireContext()).apply {
                    setTitle(getString(R.string.create_folder_label))
                    setView(view)
                    setPositiveButton("确认", null)
                    setNegativeButton("取消", null)
                }.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val newFolder = editText.text.toString()
                    when {
                        newFolder == "" ->
                            Toast.makeText(requireContext(), "内容为空", Toast.LENGTH_SHORT)
                                .show()
                        Regex("""[\\/:*?"<>|]""").containsMatchIn(newFolder) ->
                            Toast.makeText(requireContext(), "文件名不合法", Toast.LENGTH_SHORT)
                                .show()
                        else -> {
                            val newFile = Paths.get(folderPath + File.separator + newFolder)
                            if (Files.exists(newFile)) {
                                Toast.makeText(
                                    requireContext(),
                                    "文件已经存在",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                return@setOnClickListener
                            }
                            Files.createDirectory(newFile)
                            viewModel.initData(Paths.get(folderPath!!))
                            dialog.dismiss()
                        }
                    }
                }
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }


    companion object {
        const val CURRENT_PATH = "CURRENT_PATH"

        @JvmStatic
        fun newInstance(folderPath: String) =
            FileListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FOLDER_PATH, folderPath)
                }
            }
    }
}