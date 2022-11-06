package bd.emon.movies.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import bd.emon.movies.RxImmediateSchedulerRule
import bd.emon.movies.any
import bd.emon.movies.cache.MovieCacheRepository
import bd.emon.movies.capture
import bd.emon.movies.common.ASyncTransformer
import bd.emon.movies.common.DEFAULT_ORDER_BY
import bd.emon.movies.common.NETWORK_ERROR_DEFAULT
import bd.emon.movies.common.NO_DATA_ERR
import bd.emon.movies.common.PARAM_API_KEY
import bd.emon.movies.common.PARAM_GENRES
import bd.emon.movies.common.PARAM_INCLUDE_ADULT
import bd.emon.movies.common.PARAM_LANGUAGE
import bd.emon.movies.common.PARAM_PAGE
import bd.emon.movies.common.PARAM_RELEASE_YEAR
import bd.emon.movies.common.PARAM_SORT_BY
import bd.emon.movies.common.PARAM_VOTE_COUNT_GREATER_THAN
import bd.emon.movies.common.SAVE_TO_PREF_ERROR_DEFAULT
import bd.emon.movies.entity.Optional
import bd.emon.movies.fakeData.MovieApiDummyDataProvider
import bd.emon.movies.rest.MovieRestRepository
import bd.emon.movies.usecase.GetCacheDiscoverMovieFilterUseCase
import bd.emon.movies.usecase.GetDiscoverMoviesUseCase
import bd.emon.movies.usecase.GetGenresUseCase
import bd.emon.movies.usecase.SaveCacheDiscoverMoviesFiltersUseCase
import io.reactivex.rxjava3.core.Observable
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.anyMap
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class HomeViewModelTest {
    @get:Rule
    val schedulers = RxImmediateSchedulerRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()
    val API_KEY = "api_key"
    val LANG = "lang"
    val SORT_BY = "sort_by.desc"
    val MOVIE_RELEASE_YEAR = "releaseYr"
    val INCLUDE_ADULT = true
    val PAGE = 11
    val VOTE_COUNT_GREATER_THAN = 10000
    val GENRE = 28

    @Captor
    lateinit var mapCaptor: ArgumentCaptor<HashMap<String, Any?>>

    @Captor
    lateinit var stringCaptor: ArgumentCaptor<String>

    @Captor
    lateinit var intingCaptor: ArgumentCaptor<Int>

    @Captor
    lateinit var booleaningCaptor: ArgumentCaptor<Boolean>

    @Mock
    lateinit var movieRestRepository: MovieRestRepository

    @Mock
    lateinit var movieCacheRepository: MovieCacheRepository
    lateinit var getGenresUseCase: GetGenresUseCase
    lateinit var getDiscoverMoviesUseCase: GetDiscoverMoviesUseCase
    lateinit var saveCacheDiscoverMoviesFiltersUseCase: SaveCacheDiscoverMoviesFiltersUseCase
    lateinit var getCacheDiscoverMovieFilterUseCase: GetCacheDiscoverMovieFilterUseCase
    lateinit var homeViewModel: HomeViewModel

    @Before
    fun setUp() {
        getGenresUseCase = GetGenresUseCase(ASyncTransformer(), movieRestRepository)
        getDiscoverMoviesUseCase = GetDiscoverMoviesUseCase(ASyncTransformer(), movieRestRepository)
        saveCacheDiscoverMoviesFiltersUseCase =
            SaveCacheDiscoverMoviesFiltersUseCase(ASyncTransformer(), movieCacheRepository)
        getCacheDiscoverMovieFilterUseCase =
            GetCacheDiscoverMovieFilterUseCase(ASyncTransformer(), movieCacheRepository)
        homeViewModel = HomeViewModel(
            getGenresUseCase = getGenresUseCase,
            getDiscoverMoviesUseCase = getDiscoverMoviesUseCase,
            saveCacheDiscoverMoviesFiltersUseCase = saveCacheDiscoverMoviesFiltersUseCase,
            getCacheDiscoverMovieFilterUseCase = getCacheDiscoverMovieFilterUseCase,
            API_KEY,
            LANG
        )
        getGenres_success()
        getDiscoverMovies_success()
        saveDiscoverFilters_success()
    }

    @Test
    fun loadGenres_rightParamsPassedToUseCase() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        homeViewModel.loadGenres(params)
        assertThat(getGenresUseCase.params?.get(PARAM_API_KEY), `is`(API_KEY))
        assertThat(getGenresUseCase.params?.get(PARAM_LANGUAGE), `is`(LANG))
    }

    @Test
    fun loadGenres_rightParamsPassedToRepository() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        homeViewModel.loadGenres(params)
        verify(movieRestRepository, times(1)).getGenres(capture(mapCaptor))
        assertThat(mapCaptor.value[PARAM_API_KEY], `is`(API_KEY))
        assertThat(mapCaptor.value[PARAM_LANGUAGE], `is`(LANG))
    }

    @Test
    fun loadGenres_success_genreListEmitted() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        homeViewModel.loadGenres(params)
        assertThat(homeViewModel.genres.value!! == MovieApiDummyDataProvider.genreList, `is`(true))
    }

    @Test
    fun loadGenres_success_loadStateFalseEmitted() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        homeViewModel.loadGenres(params)
        assertThat(homeViewModel.loadingState.value!! == false, `is`(true))
    }

    @Test
    fun loadGenres_success_emptyListNoDataMessageEmitted() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        getGenres_noData()
        homeViewModel.loadGenres(params)
        assertThat(homeViewModel.genreErrorState.value!!.message == NO_DATA_ERR, `is`(true))
    }

    @Test
    fun loadGenres_success_emptyListEmitted() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        getGenres_noData()
        homeViewModel.loadGenres(params)
        assertThat(homeViewModel.genres.value, nullValue())
    }

    @Test
    fun loadGenres_networkError_errorEmitted() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        getGenres_networkErr()
        homeViewModel.loadGenres(params)
        assertThat(homeViewModel.genreErrorState.value!!.message, `is`(NETWORK_ERROR_DEFAULT))
    }

    @Test
    fun loadGenres_networkError_LoadStateFalseEmitted() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        getGenres_networkErr()
        homeViewModel.loadGenres(params)
        assertThat(homeViewModel.loadingState.value == false, `is`(true))
    }

    @Test
    fun loadGenres_networkError_emptyListEmitted() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        getGenres_networkErr()
        homeViewModel.loadGenres(params)
        assertThat(homeViewModel.genres.value, nullValue())
    }

    @Test
    fun loadDiscoverMovies_correctParams_apiKey_language_genre_passedToUseCase() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)
        assertThat(getDiscoverMoviesUseCase.params[PARAM_API_KEY], `is`(API_KEY))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_LANGUAGE], `is`(LANG))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_GENRES], `is`(GENRE))
    }

    @Test
    fun loadDiscoverMovies_correctParams_apiKey_language_genre_sortBy_passedToUseCase() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        params[PARAM_SORT_BY] = SORT_BY
        homeViewModel.loadDiscoverMovies(params)
        assertThat(getDiscoverMoviesUseCase.params[PARAM_API_KEY], `is`(API_KEY))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_LANGUAGE], `is`(LANG))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_GENRES], `is`(GENRE))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_SORT_BY], `is`(SORT_BY))
    }

    @Test
    fun loadDiscoverMovies_correctParams_apiKey_language_genre_sortBy_includeAdult_passedToUseCase() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        params[PARAM_SORT_BY] = SORT_BY
        params[PARAM_INCLUDE_ADULT] = INCLUDE_ADULT
        homeViewModel.loadDiscoverMovies(params)
        assertThat(getDiscoverMoviesUseCase.params[PARAM_API_KEY], `is`(API_KEY))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_LANGUAGE], `is`(LANG))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_GENRES], `is`(GENRE))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_SORT_BY], `is`(SORT_BY))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_INCLUDE_ADULT], `is`(INCLUDE_ADULT))
    }

    @Test
    fun loadDiscoverMovies_correctParams_apiKey_language_genre_sortBy_includeAdult_page_passedToUseCase() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        params[PARAM_SORT_BY] = SORT_BY
        params[PARAM_INCLUDE_ADULT] = INCLUDE_ADULT
        params[PARAM_PAGE] = PAGE
        homeViewModel.loadDiscoverMovies(params)
        assertThat(getDiscoverMoviesUseCase.params[PARAM_API_KEY], `is`(API_KEY))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_LANGUAGE], `is`(LANG))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_GENRES], `is`(GENRE))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_SORT_BY], `is`(SORT_BY))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_INCLUDE_ADULT], `is`(INCLUDE_ADULT))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_PAGE], `is`(PAGE))
    }

    @Test
    fun loadDiscoverMovies_correctParams_apiKey_language_genre_sortBy_includeAdult_page_voteCountGreaterThan_passedToUseCase() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        params[PARAM_SORT_BY] = SORT_BY
        params[PARAM_INCLUDE_ADULT] = INCLUDE_ADULT
        params[PARAM_PAGE] = PAGE
        params[PARAM_VOTE_COUNT_GREATER_THAN] = VOTE_COUNT_GREATER_THAN
        homeViewModel.loadDiscoverMovies(params)
        assertThat(getDiscoverMoviesUseCase.params[PARAM_API_KEY], `is`(API_KEY))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_LANGUAGE], `is`(LANG))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_GENRES], `is`(GENRE))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_SORT_BY], `is`(SORT_BY))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_INCLUDE_ADULT], `is`(INCLUDE_ADULT))
        assertThat(getDiscoverMoviesUseCase.params[PARAM_PAGE], `is`(PAGE))
        assertThat(
            getDiscoverMoviesUseCase.params[PARAM_VOTE_COUNT_GREATER_THAN],
            `is`(VOTE_COUNT_GREATER_THAN)
        )
    }

    @Test
    fun loadDiscoverMovies_correctParams_apiKey_language_genre_passedToRepository() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)
        verify(movieRestRepository, times(1)).getDiscoverMovies(
            capture(mapCaptor)
        )
        assertThat(mapCaptor.value[PARAM_API_KEY], `is`(API_KEY))
        assertThat(mapCaptor.value[PARAM_LANGUAGE], `is`(LANG))
        assertThat(mapCaptor.value[PARAM_GENRES], `is`(GENRE))
    }

    @Test
    fun loadDiscoverMovies_correctParams_apiKey_language_genre_sortBy_passedToRepository() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        params[PARAM_SORT_BY] = SORT_BY
        homeViewModel.loadDiscoverMovies(params)
        verify(movieRestRepository, times(1)).getDiscoverMovies(
            capture(mapCaptor)
        )
        assertThat(mapCaptor.value[PARAM_API_KEY], `is`(API_KEY))
        assertThat(mapCaptor.value[PARAM_LANGUAGE], `is`(LANG))
        assertThat(mapCaptor.value[PARAM_GENRES], `is`(GENRE))
        assertThat(mapCaptor.value[PARAM_SORT_BY], `is`(SORT_BY))
    }

    @Test
    fun loadDiscoverMovies_correctParams_apiKey_language_genre_sortBy_includeAdult_passedToRepository() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        params[PARAM_SORT_BY] = SORT_BY
        params[PARAM_INCLUDE_ADULT] = INCLUDE_ADULT
        homeViewModel.loadDiscoverMovies(params)
        verify(movieRestRepository, times(1)).getDiscoverMovies(
            capture(mapCaptor)
        )
        assertThat(mapCaptor.value[PARAM_API_KEY], `is`(API_KEY))
        assertThat(mapCaptor.value[PARAM_LANGUAGE], `is`(LANG))
        assertThat(mapCaptor.value[PARAM_GENRES], `is`(GENRE))
        assertThat(mapCaptor.value[PARAM_SORT_BY], `is`(SORT_BY))
        assertThat(mapCaptor.value[PARAM_INCLUDE_ADULT], `is`(INCLUDE_ADULT))
    }

    @Test
    fun loadDiscoverMovies_correctParams_apiKey_language_genre_sortBy_includeAdult_page_passedToRepository() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        params[PARAM_SORT_BY] = SORT_BY
        params[PARAM_INCLUDE_ADULT] = INCLUDE_ADULT
        params[PARAM_PAGE] = PAGE
        homeViewModel.loadDiscoverMovies(params)
        verify(movieRestRepository, times(1)).getDiscoverMovies(
            capture(mapCaptor)
        )
        assertThat(mapCaptor.value[PARAM_API_KEY], `is`(API_KEY))
        assertThat(mapCaptor.value[PARAM_LANGUAGE], `is`(LANG))
        assertThat(mapCaptor.value[PARAM_GENRES], `is`(GENRE))
        assertThat(mapCaptor.value[PARAM_SORT_BY], `is`(SORT_BY))
        assertThat(mapCaptor.value[PARAM_INCLUDE_ADULT], `is`(INCLUDE_ADULT))
        assertThat(mapCaptor.value[PARAM_PAGE], `is`(PAGE))
    }

    @Test
    fun loadDiscoverMovies_correctParams_apiKey_language_genre_sortBy_includeAdult_page_voteCountGreaterThan_passedToRepository() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        params[PARAM_SORT_BY] = SORT_BY
        params[PARAM_INCLUDE_ADULT] = INCLUDE_ADULT
        params[PARAM_PAGE] = PAGE
        params[PARAM_VOTE_COUNT_GREATER_THAN] = VOTE_COUNT_GREATER_THAN
        homeViewModel.loadDiscoverMovies(params)
        verify(movieRestRepository, times(1)).getDiscoverMovies(
            capture(mapCaptor)
        )
        assertThat(mapCaptor.value[PARAM_API_KEY], `is`(API_KEY))
        assertThat(mapCaptor.value[PARAM_LANGUAGE], `is`(LANG))
        assertThat(mapCaptor.value[PARAM_GENRES], `is`(GENRE))
        assertThat(mapCaptor.value[PARAM_SORT_BY], `is`(SORT_BY))
        assertThat(mapCaptor.value[PARAM_INCLUDE_ADULT], `is`(INCLUDE_ADULT))
        assertThat(mapCaptor.value[PARAM_PAGE], `is`(PAGE))
        assertThat(mapCaptor.value[PARAM_VOTE_COUNT_GREATER_THAN], `is`(VOTE_COUNT_GREATER_THAN))
    }

    @Test
    fun loadDiscoverMovies_success_listEmitted() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)
        assertThat(
            homeViewModel.discoverMovies.value,
            `is`(MovieApiDummyDataProvider.disocoverMovies)
        )
    }

    @Test
    fun loadDiscoverMovies_success_correct_grp_gen_idEmitted() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)
        assertThat(homeViewModel.discoverMovies.value!!.grp_genre_id, `is`(GENRE))
    }

    @Test
    fun loadDiscoverMovies_success_loadStateFalseEmitted() {
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)
        assertThat(
            homeViewModel.loadingState.value,
            `is`(false)
        )
    }

    @Test
    fun loadDiscoverMovies_successEmptyList_nullEmitted() {
        getDiscoverMovies_noData()
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)
        assertThat(
            homeViewModel.discoverMovies.value,
            `is`(nullValue())
        )
    }

    @Test
    fun loadDiscoverMovies_successEmptyList_noDataErrorMessageEmitted() {
        getDiscoverMovies_noData()
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)

        assertThat(
            homeViewModel.discoverMoviesErrorState.value!!.errorMessage == NO_DATA_ERR,
            `is`(true)
        )
    }

    @Test
    fun loadDiscoverMovies_successEmptyList_correctGrpGenreIdInThrowableEmitted() {
        getDiscoverMovies_noData()
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)

        assertThat(
            homeViewModel.discoverMoviesErrorState.value!!.grp_genre_id == GENRE,
            `is`(true)
        )
    }

    @Test
    fun loadDiscoverMovies_successEmptyList_loadStateFalseEmitted() {
        getDiscoverMovies_noData()
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)
        assertThat(homeViewModel.loadingState.value, `is`(false))
    }

    @Test
    fun loadDiscoverMovies_networkError_nullEmitted() {
        getDiscoverMovies_networkError()
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)
        assertThat(homeViewModel.discoverMovies.value, `is`(nullValue()))
    }

    @Test
    fun loadDiscoverMovies_networkError_networkErrorMessageEmitted() {
        getDiscoverMovies_networkError()
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)

        assertThat(
            homeViewModel.discoverMoviesErrorState.value!!.errorMessage,
            `is`(NETWORK_ERROR_DEFAULT)
        )
    }

    @Test
    fun loadDiscoverMovies_networkError_networkError_correctGrpGenreIdInThrowableEmitted() {
        getDiscoverMovies_networkError()
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)

        assertThat(
            homeViewModel.discoverMoviesErrorState.value!!.grp_genre_id,
            `is`(GENRE)
        )
    }

    @Test
    fun loadDiscoverMovies_networkError_loadStateFalseEmitted() {
        getDiscoverMovies_networkError()
        val params: HashMap<String, Any?> = hashMapOf()
        params[PARAM_API_KEY] = API_KEY
        params[PARAM_LANGUAGE] = LANG
        params[PARAM_GENRES] = GENRE
        homeViewModel.loadDiscoverMovies(params)
        assertThat(homeViewModel.loadingState.value, `is`(false))
    }

    @Test
    fun saveDiscoverMoviesFilter_correctParamsPassedToUseCase() {
        homeViewModel.saveDiscoverMoviesFilters(
            minVoteCount = VOTE_COUNT_GREATER_THAN,
            includeAdultContent = INCLUDE_ADULT,
            orderBy = SORT_BY,
            releaseYearStr = MOVIE_RELEASE_YEAR
        )
        assertThat(
            saveCacheDiscoverMoviesFiltersUseCase.minVoteCount,
            `is`(VOTE_COUNT_GREATER_THAN)
        )
        assertThat(saveCacheDiscoverMoviesFiltersUseCase.includeAdultContent, `is`(INCLUDE_ADULT))
        assertThat(saveCacheDiscoverMoviesFiltersUseCase.orderBy, `is`(SORT_BY))
        assertThat(saveCacheDiscoverMoviesFiltersUseCase.releaseYearStr, `is`(MOVIE_RELEASE_YEAR))
    }

    @Test
    fun saveDiscoverMoviesFilter_correctParamsPassedToRepository() {
        homeViewModel.saveDiscoverMoviesFilters(
            minVoteCount = VOTE_COUNT_GREATER_THAN,
            includeAdultContent = INCLUDE_ADULT,
            orderBy = SORT_BY,
            releaseYearStr = MOVIE_RELEASE_YEAR
        )
        verify(movieCacheRepository, times(1))
            .saveDiscoverMovieFilters(
                capture(intingCaptor),
                capture(booleaningCaptor),
                capture(stringCaptor),
                capture(stringCaptor)
            )
        assertThat(intingCaptor.allValues[0], `is`(VOTE_COUNT_GREATER_THAN))
        assertThat(booleaningCaptor.allValues[0], `is`(INCLUDE_ADULT))
        assertThat(stringCaptor.allValues[0], `is`(SORT_BY))
        assertThat(stringCaptor.allValues[1], `is`(MOVIE_RELEASE_YEAR))
    }

    @Test
    fun saveDiscoverMoviesFilter_success_mutablePreferenceEmitted() {
        homeViewModel.saveDiscoverMoviesFilters(
            minVoteCount = VOTE_COUNT_GREATER_THAN,
            includeAdultContent = INCLUDE_ADULT,
            orderBy = SORT_BY,
            releaseYearStr = MOVIE_RELEASE_YEAR
        )

        assertThat(
            homeViewModel.discoverFilters.value,
            `is`(MovieApiDummyDataProvider.discoverFilters)
        )
    }

    @Test
    fun saveDiscoverMoviesFilter_error_nullEmitted() {
        saveDiscoverFilters_error()
        homeViewModel.saveDiscoverMoviesFilters(
            minVoteCount = VOTE_COUNT_GREATER_THAN,
            includeAdultContent = INCLUDE_ADULT,
            orderBy = SORT_BY,
            releaseYearStr = MOVIE_RELEASE_YEAR
        )

        assertThat(
            homeViewModel.discoverFilters.value,
            `is`(nullValue())
        )
    }

    @Test
    fun saveDiscoverMoviesFilter_error_prefErrorMessageEmitted() {
        saveDiscoverFilters_error()
        homeViewModel.saveDiscoverMoviesFilters(
            minVoteCount = VOTE_COUNT_GREATER_THAN,
            includeAdultContent = INCLUDE_ADULT,
            orderBy = SORT_BY,
            releaseYearStr = MOVIE_RELEASE_YEAR
        )

        assertThat(
            homeViewModel.discoverFiltersErrorState.value!!.message,
            `is`(SAVE_TO_PREF_ERROR_DEFAULT)
        )
    }

    @Test
    fun loadDiscoverMovieFiltersAndHoldInApiParamMap_notCached_emptyPrefEmitted() {
        getDiscoverMovieFilters_notCached()
        homeViewModel.loadDiscoverMovieFiltersAndHoldInApiParamMap()
        assertThat(
            homeViewModel.discoverFilters.value,
            `is`(MovieApiDummyDataProvider.discoverDefaultFilters)
        )
    }

    @Test
    fun loadDiscoverMovieFiltersAndHoldInApiParamMap_notCached_defaultValueHoldInApiParamMap() {
        getDiscoverMovieFilters_notCached()
        homeViewModel.loadDiscoverMovieFiltersAndHoldInApiParamMap()

        assertThat(homeViewModel.apiParams[PARAM_API_KEY], `is`(API_KEY))
        assertThat(homeViewModel.apiParams[PARAM_LANGUAGE], `is`(LANG))
        assertThat(homeViewModel.apiParams[PARAM_SORT_BY], `is`(DEFAULT_ORDER_BY))
        assertThat(homeViewModel.apiParams[PARAM_VOTE_COUNT_GREATER_THAN], `is`(0))
        assertThat(homeViewModel.apiParams[PARAM_INCLUDE_ADULT], `is`(false))
        assertThat(homeViewModel.apiParams[PARAM_RELEASE_YEAR], `is`(nullValue()))
    }

    @Test
    fun loadDiscoverMovieFiltersAndHoldInApiParamMap_cached_prefWitchCachedDataEmitted() {
        getDiscoverMovieFilters_cached()
        homeViewModel.loadDiscoverMovieFiltersAndHoldInApiParamMap()
        assertThat(
            homeViewModel.discoverFilters.value,
            `is`(MovieApiDummyDataProvider.discoverFilters)
        )
    }

    @Test
    fun loadDiscoverMovieFiltersAndHoldInApiParamMap_cached_defaultValueHoldInApiParamMap() {
        getDiscoverMovieFilters_cached()
        homeViewModel.loadDiscoverMovieFiltersAndHoldInApiParamMap()

        assertThat(homeViewModel.apiParams[PARAM_API_KEY], `is`(API_KEY))
        assertThat(homeViewModel.apiParams[PARAM_LANGUAGE], `is`(LANG))
        assertThat(homeViewModel.apiParams[PARAM_SORT_BY], `is`(SORT_BY))
        assertThat(
            homeViewModel.apiParams[PARAM_VOTE_COUNT_GREATER_THAN],
            `is`(VOTE_COUNT_GREATER_THAN)
        )
        assertThat(homeViewModel.apiParams[PARAM_INCLUDE_ADULT], `is`(INCLUDE_ADULT))
        assertThat(homeViewModel.apiParams[PARAM_RELEASE_YEAR], `is`(MOVIE_RELEASE_YEAR))
    }

    //region helper methods
    fun getGenres_success() {
        `when`(movieRestRepository.getGenres(anyMap()))
            .thenReturn(Observable.just(Optional.of(MovieApiDummyDataProvider.genreList)))
    }

    fun getGenres_networkErr() {
        `when`(movieRestRepository.getGenres(anyMap()))
            .thenThrow(RuntimeException(NETWORK_ERROR_DEFAULT))
    }

    fun getGenres_noData() {
        `when`(movieRestRepository.getGenres(anyMap()))
            .thenReturn(Observable.just(Optional.empty()))
    }

    fun getDiscoverMovies_success() {
        `when`(
            movieRestRepository.getDiscoverMovies(anyMap())
        )
            .thenReturn(Observable.just(Optional.of(MovieApiDummyDataProvider.disocoverMovies)))
    }

    fun getDiscoverMovies_noData() {
        `when`(movieRestRepository.getDiscoverMovies(anyMap()))
            .thenReturn(Observable.just(Optional.empty()))
    }

    fun getDiscoverMovies_networkError() {
        `when`(movieRestRepository.getDiscoverMovies(anyMap())).thenThrow(
            RuntimeException(
                NETWORK_ERROR_DEFAULT
            )
        )
    }

    fun saveDiscoverFilters_success() {
        `when`(
            movieCacheRepository.saveDiscoverMovieFilters(
                any(Int::class.java),
                any(Boolean::class.java),
                any(String::class.java),
                any(String::class.java)
            )
        )
            .thenReturn(Observable.just(Optional.of(MovieApiDummyDataProvider.discoverFilters)))
    }

    fun saveDiscoverFilters_error() {
        `when`(
            movieCacheRepository.saveDiscoverMovieFilters(
                any(Int::class.java),
                any(Boolean::class.java),
                any(String::class.java),
                any(String::class.java)
            )
        )
            .thenThrow(RuntimeException(SAVE_TO_PREF_ERROR_DEFAULT))
    }

    fun getDiscoverMovieFilters_notCached() {
        `when`(
            movieCacheRepository.getDiscoverMovieFilters()
        )
            .thenReturn(Observable.just(Optional.of(null)))
    }

    fun getDiscoverMovieFilters_cached() {
        `when`(
            movieCacheRepository.getDiscoverMovieFilters()
        )
            .thenReturn(Observable.just(Optional.of(MovieApiDummyDataProvider.discoverFilters)))
    }
    //endregion
}
