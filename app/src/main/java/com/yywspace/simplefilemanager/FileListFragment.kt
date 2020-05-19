package com.yywspace.simplefilemanager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.yywspace.simplefilemanager.adapters.FileListAdapter
import com.yywspace.simplefilemanager.viewmodels.FileListViewModel
import com.yywspace.simplefilemanager.viewmodels.FileListViewModelFactory
import kotlinx.android.synthetic.main.dialog_file_list_sort_layout.view.*
import kotlinx.android.synthetic.main.fragment_file_list.*
import kotlinx.android.synthetic.main.fragment_file_list_container.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

private const val ARG_FOLDER_PATH = "folder_path"

class FileListFragment : Fragment() {
    private val TAG = "FileListFragment"
    var isCut = false
    private val viewModel: FileListViewModel by viewModels {
        FileListViewModelFactory(requireActivity().application, this)
    }


    private var folderPath: String? = null
    private lateinit var adapter: FileListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            folderPath = it.getString(ARG_FOLDER_PATH)
        }
        setHasOptionsMenu(true)
    }

    fun initData() {
        Log.d(TAG, "initData123: ${folderPath}")
        viewModel.initData(Paths.get(folderPath!!), true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_list, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.file_list_page_label)
    }

    var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_file_select, menu)
            requireActivity().window.statusBarColor =
                resources.getColor(R.color.colorToolbar, null)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            (parentFragment as FileListContainerFragment).crumbView.setItemsClickable(false)
            FileListViewModel.isActionModeOn = true
            adapter.isMultiSelect = true
            adapter.onItemClickListener = { file, index ->
                if (file.selected)
                    viewModel.unSelect(index)
                else
                    viewModel.select(index)
            }
            return true
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_select_all -> {
                    Toast.makeText(
                        requireContext(),
                        "${FileListViewModel.currentPath}",
                        Toast.LENGTH_SHORT
                    ).show()
                    item.isChecked = !item.isChecked
                    if (item.isChecked) {
                        item.setIcon(R.drawable.ic_unselect_all)
                        viewModel.selectAll()
                    } else {
                        item.setIcon(R.drawable.ic_select_all)
                        viewModel.unSelectAll()
                    }
                }
                R.id.action_delete -> {
                    AlertDialog.Builder(requireContext()).apply {
                        setTitle(R.string.delete_file_title)
                        setMessage(
                            getString(
                                R.string.delete_file_message,
                                viewModel.getSelectList()?.size
                            )
                        )
                        setPositiveButton(R.string.confirm_label) { _, _ ->
                            viewModel.deleteSelected {
                                viewModel.initData(Paths.get(folderPath!!), true)
                            }
                            actionMode?.finish()
                        }
                        setNegativeButton(R.string.cancel_label, null)
                        show()
                    }
                }
                R.id.action_revert_select ->
                    viewModel.reverseSelect()
                R.id.action_paste -> {
                    Log.d("BasicSortViewModel", "action_paste: ${viewModel.getLiveData().value}")
                    if (isCut) {
                        viewModel.cutSelectedTo(FileListViewModel.currentPath!!) {
                            if (parentFragment == null) {
                                (FileListContainerFragment.getFirstFragment() as FileListFragment).initData()
                            } else
                                (parentFragment as FileListContainerFragment).childFragmentManager.fragments.apply {
                                    (get(size - 1) as FileListFragment).initData()
                                }
                        }
                    } else {
                        viewModel.copySelectedTo(FileListViewModel.currentPath!!) {
                            if (parentFragment == null) {
                                (FileListContainerFragment.getFirstFragment() as FileListFragment).initData()
                            } else
                                (parentFragment as FileListContainerFragment).childFragmentManager.fragments.apply {
                                    (get(size - 1) as FileListFragment).initData()
                                }
                        }
                    }
                    actionMode?.finish()

//                    Toast.makeText(
//                        requireContext(),
//                        "${FileListViewModel.currentPath}",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
                R.id.action_cut, R.id.action_copy -> {
                    (parentFragment as FileListContainerFragment).crumbView.setItemsClickable(true)
                    isCut = R.id.action_cut == item.itemId
                    actionMode?.title = "请选择路径"
                    actionMode?.menu?.forEachIndexed { id, menuItem ->
                        menuItem.isVisible = !menuItem.isVisible
                    }
                    Log.d("BasicSortViewModel", "action_copy: ${viewModel.getLiveData().value}")
                    adapter.isMultiSelect = false
                    adapter.notifyDataSetChanged()
                    adapter.onItemClickListener = { file, i ->
                        if (viewModel.isSelected(i)!!) {
                            Toast.makeText(
                                requireContext(),
                                "当前目录已选中",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (Files.isDirectory(file.path))
                                (parentFragment as FileListContainerFragment)
                                    .addCrumbItem(
                                        file.path.fileName.toString(),
                                        newInstance(file.path.toFile().absolutePath),
                                        file.path.toAbsolutePath().toString()
                                    )
                        }
                    }
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            FileListViewModel.isActionModeOn = false
            parentFragment?.apply {
                (this as FileListContainerFragment).crumbView.setItemsClickable(true)
            }
            viewModel.unSelectAll()
            adapter.isMultiSelect = false
            adapter.notifyDataSetChanged()
            adapter.onItemClickListener = { file, _ ->
                Toast.makeText(requireContext(), file.path.toFile().extension, Toast.LENGTH_SHORT)
                    .show()
                if (Files.isDirectory(file.path))
                    (parentFragment as FileListContainerFragment)
                        .addCrumbItem(
                            file.path.fileName.toString(),
                            newInstance(file.path.toFile().absolutePath),
                            file.path.toAbsolutePath().toString()
                        )
            }
            actionMode = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FileListAdapter().apply {
            onItemClickListener = { file, _ ->
                Toast.makeText(requireContext(), file.path.toFile().extension, Toast.LENGTH_SHORT)
                    .show()
                if (Files.isDirectory(file.path))
                    (parentFragment as FileListContainerFragment)
                        .addCrumbItem(
                            file.path.fileName.toString(),
                            newInstance(file.path.toFile().absolutePath),
                            file.path.toAbsolutePath().toString()
                        )
            }
            onItemLongClickListener = { _, i ->
                // Called when the user long-clicks on someView
                when (actionMode) {
                    null -> {
                        // Start the CAB using the ActionMode.Callback defined above
                        actionMode = activity?.startActionMode(actionModeCallback)
                        viewModel.select(i)
                        true
                    }
                    else -> false
                }
            }
        }

        fileRecyclerView.apply {
            adapter = this@FileListFragment.adapter
            layoutManager = WrapContentLinearLayoutManager(requireActivity())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }
        viewModel.initData(Paths.get(folderPath!!), !FileListViewModel.isActionModeOn)
        viewModel.getLiveData().observe(viewLifecycleOwner, Observer { pathList ->
            adapter.submitList(pathList)
            if (!adapter.isMultiSelect)
                fileRecyclerView.apply {
                    postDelayed({
                        (layoutManager as LinearLayoutManager)
                            .scrollToPositionWithOffset(0, 0)
                    }, 100)
                }
            else
                adapter.notifyDataSetChanged()
            actionMode?.title = viewModel.getSelectList()?.size.toString()
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

                            if (!viewModel.createFolder(newFile)) {
                                Toast.makeText(
                                    requireContext(),
                                    "文件已经存在",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }
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
