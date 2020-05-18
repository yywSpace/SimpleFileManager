package com.yywspace.simplefilemanager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.google.android.material.snackbar.Snackbar
import com.yywspace.simplefilemanager.adapters.FileListAdapter
import com.yywspace.simplefilemanager.adapters.SearchHistoryAdapter
import com.yywspace.simplefilemanager.data.SearchHistory
import com.yywspace.simplefilemanager.viewmodels.FileSearchViewModel
import com.yywspace.simplefilemanager.viewmodels.SearchMode
import com.yywspace.simplefilemanager.viewmodels.SearchStatus
import kotlinx.android.synthetic.main.dialog_file_list_sort_layout.view.*
import kotlinx.android.synthetic.main.fragment_file_search.*
import java.nio.file.Paths
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class FileSearchFragment : Fragment() {
    private val TAG = "FileSearchFragment"
    private lateinit var fileAdapter: FileListAdapter
    private lateinit var historyAdapter: SearchHistoryAdapter
    private lateinit var searchView: SearchView
    private var currentPath: String? = null
    val viewModel: FileSearchViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        currentPath = arguments?.getString(FileListFragment.CURRENT_PATH)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyAdapter = SearchHistoryAdapter().apply {
            onItemClickListener = {
                hideInputKeyboard()
                search(it.historyContent)
                it.historyTime = Calendar.getInstance()
                viewModel.insertOrUpdate(it)
                searchView.setQuery(it.historyContent, false)
            }
            onClearButtonClickListener = {
                viewModel.clearHistory()
            }
            onDeleteButtonClickListener = { history, view ->
                view.postDelayed({
                    viewModel.deleteHistory(history)
                }, 100)
            }
        }
        fileAdapter = FileListAdapter()
        fileSearchList.apply {
            adapter = this@FileSearchFragment.historyAdapter
            layoutManager = WrapContentLinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }
        searchStatusLabel.setOnClickListener {
            Snackbar.make(
                it,
                getString(
                    R.string.file_search_list_size_out_of_limit,
                    FileSearchViewModel.MAX_FILE_SIZE,
                    FileSearchViewModel.MAX_FILE_SIZE
                ), Snackbar.LENGTH_LONG
            ).show()
        }
        searchHistoryLabel.setOnClickListener {
            when (fileSearchList.adapter) {
                is SearchHistoryAdapter -> {
                    Snackbar.make(it, R.string.search_history_label, Snackbar.LENGTH_LONG).show()
                }
                is FileListAdapter -> {
                    when (viewModel.getSearchMode()) {
                        SearchMode.GLOBAL ->
                            Snackbar.make(
                                it,
                                R.string.global_search_label_hint,
                                Snackbar.LENGTH_LONG
                            ).show()
                        SearchMode.LOCAL ->
                            Snackbar.make(
                                it,
                                getString(R.string.local_search_label_hint, currentPath),
                                Snackbar.LENGTH_LONG
                            ).show()
                    }
                }
            }
        }
        viewModel.initData(Paths.get(currentPath!!))
        viewModel.getLiveData().observe(viewLifecycleOwner, Observer { searchList ->
            fileAdapter.submitList(listOf())
            fileAdapter.notifyDataSetChanged()
            fileAdapter.submitList(searchList)
            fileSearchList.apply {
                postDelayed({
                    (layoutManager as LinearLayoutManager)
                        .scrollToPositionWithOffset(0, 0)
                }, 500)
            }
        })

        viewModel.getHistory().observe(viewLifecycleOwner, Observer { searchHistories ->
            Log.d(TAG, "onViewCreated: ${searchHistories.size}")
            historyAdapter.submitList(searchHistories)
        })

        viewModel.searchStatus.observe(viewLifecycleOwner, Observer { searchStatus ->
            searchStatusLabel.text = when (searchStatus!!) {
                SearchStatus.SEARCHING -> {
                    searchStatusProcessBar.visibility = View.VISIBLE
                    getString(R.string.search_status_searching_label)
                }
                SearchStatus.FINISH -> {
                    if (fileAdapter.currentList.size >= FileSearchViewModel.MAX_FILE_SIZE)
                        Snackbar.make(
                            searchStatusLabel,
                            getString(
                                R.string.file_search_list_size_out_of_limit,
                                FileSearchViewModel.MAX_FILE_SIZE,
                                FileSearchViewModel.MAX_FILE_SIZE
                            ), Snackbar.LENGTH_LONG
                        ).show()
                    searchStatusProcessBar.visibility = View.INVISIBLE
                    getString(R.string.search_status_finish_label)
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_file_search, menu)
        menu.findItem(R.id.global_search).apply {
            isChecked = when (viewModel.getSearchMode()) {
                SearchMode.GLOBAL -> true
                // local
                else -> false
            }
        }
        menu.findItem(R.id.recursive_search).isChecked = viewModel.isRecursiveSearch()
        menu.findItem(R.id.search).apply {
            expandActionView()
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    NavHostFragment.findNavController(this@FileSearchFragment)
                        .navigateUp()
                    return true
                }

            })
            searchView = (actionView as SearchView).apply {
                isSubmitButtonEnabled = true
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        if (query != null) {
                            search(query)
                            viewModel.insertOrUpdate(SearchHistory(historyContent = query))
                        }
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (newText?.length == 0) {
                            fileSearchList.adapter = historyAdapter
                            searchHistoryLabel.text = getString(R.string.search_history_label)
                        }
                        return false
                    }
                })
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    @SuppressLint("InflateParams")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sortType = viewModel.getCurrentSortType()
        val sortOrder = viewModel.getCurrentSortOrder()

        return when (item.itemId) {
            R.id.sort -> {
                if (fileSearchList.adapter is SearchHistoryAdapter) {
                    Toast.makeText(requireContext(), "搜索历史界面无法排序", Toast.LENGTH_SHORT).show()
                    return false
                }
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

            R.id.unknown_file -> {
                item.isChecked = !item.isChecked
                if (item.isChecked)
                    viewModel.setUnknownFile(item.isChecked)
                else
                    viewModel.setUnknownFile(item.isChecked)
                true
            }
            R.id.global_search -> {
                item.isChecked = !item.isChecked
                if (item.isChecked) {
                    if (fileSearchList.adapter is FileListAdapter)
                        searchHistoryLabel.text = getString(R.string.global_search_label)
                    viewModel.setSearchMode(SearchMode.GLOBAL)
                } else {
                    if (fileSearchList.adapter is FileListAdapter)
                        searchHistoryLabel.text = getString(R.string.local_search_label)
                    viewModel.setSearchMode(SearchMode.LOCAL)
                }
                true
            }
            R.id.recursive_search -> {
                item.isChecked = !item.isChecked
                viewModel.setRecursiveSearch(item.isChecked)
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    fun search(query: String) {
        fileAdapter.submitList(listOf())
        when (viewModel.getSearchMode()) {
            SearchMode.GLOBAL -> {
                searchHistoryLabel.text = getString(R.string.global_search_label)
                viewModel.searchInRootPath(query)
            }
            // local
            else -> {
                searchHistoryLabel.text = getString(R.string.local_search_label)
                viewModel.searchInCurrentPath(query)
            }
        }
        fileSearchList.adapter = fileAdapter
    }

    fun hideInputKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)  as InputMethodManager
        val view: View = requireActivity().window.peekDecorView()
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

/**
 * android自身bug，此时表现为从空布局转换为含有数据的布局时出现 IndexOutOfBoundsException 捕获即可
 * https://stackoverflow.com/questions/35653439/recycler-view-inconsistency-detected-invalid-view-holder-adapter-positionviewh
 */
class WrapContentLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {
    override fun onLayoutChildren(
        recycler: Recycler,
        state: RecyclerView.State
    ) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Log.e("Error", "IndexOutOfBoundsException in RecyclerView happens")
        }
    }
}
