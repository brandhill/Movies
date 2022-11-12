package bd.emon.movies.home

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import bd.emon.movies.R
import bd.emon.movies.common.DEFAULT_VIEW_RESIZE_MARGIN
import bd.emon.movies.common.INVALID_VIEW_HOLDER
import bd.emon.movies.common.paging.PagingHelper
import bd.emon.movies.common.view.ViewResizer
import bd.emon.movies.databinding.LayoutLoaderBinding
import bd.emon.movies.databinding.LayoutMovieEntityBinding
import bd.emon.movies.entity.common.MovieEntity

class MovieListAdapter(
    private var movies: MutableList<MovieEntity>,
    private val viewResizer: ViewResizer,
    private val shouldResize: Boolean = false,
    private val pagingEnabled: Boolean = false,
    private val pagingHelper: PagingHelper? = null
) :
    RecyclerView.Adapter<MovieListAdapter.ViewHolder>() {

    private val MOVIE_ENTITY = 1
    private val LOADER_VIEW = 2

    inner class ViewHolder :
        RecyclerView.ViewHolder {
        var movieBinding: LayoutMovieEntityBinding? = null
        var loaderBinding: LayoutLoaderBinding? = null

        constructor(binding: LayoutMovieEntityBinding) : super(binding.root) {
            movieBinding = binding
            if (shouldResize) {
                viewResizer.makeViewHalfScreenWidth(binding.thumbnail, DEFAULT_VIEW_RESIZE_MARGIN)
            }
        }

        constructor(binding: LayoutLoaderBinding) : super(binding.root) {
            loaderBinding = binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ViewBinding
        when (viewType) {
            MOVIE_ENTITY -> {
                binding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.layout_movie_entity,
                    parent,
                    false
                )
                return ViewHolder(binding as LayoutMovieEntityBinding)
            }
            LOADER_VIEW -> {
                binding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.layout_loader,
                    parent,
                    false
                )
                return ViewHolder(binding as LayoutLoaderBinding)
            }
            else -> throw IllegalStateException(INVALID_VIEW_HOLDER)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.movieBinding?.let {
            it.movie = movies[position]
        }
        holder.loaderBinding?.let {
            if (!pagingHelper!!.hasMoreData()) {
                it.root.layoutParams.height = 0
                it.progressBar.visibility = GONE
            }
            if (pagingHelper!!.hasMoreData() && position == movies.size) {
                pagingHelper.loadNextPage()
            }
        }
    }

    override fun getItemCount(): Int {
        if (pagingEnabled) {
            return movies.size + 2
        }
        return movies.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position > movies.size - 1) {
            return LOADER_VIEW
        }
        return MOVIE_ENTITY
    }

    fun addMoreMovies(entities: MutableList<MovieEntity>) {
        var notifyStartIndex = movies.size
        movies.addAll(entities)
        notifyItemRangeChanged(notifyStartIndex, entities.size)
    }

    fun hideLoaders() {
        notifyItemRangeChanged(movies.size, 2)
    }
}
