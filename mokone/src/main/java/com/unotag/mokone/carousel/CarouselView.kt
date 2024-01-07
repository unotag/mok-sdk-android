package com.unotag.mokone.carousel


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.unotag.mokone.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CarouselView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var infiniteViewPager: ViewPager2
    private lateinit var infiniteRecyclerAdapter: InfiniteRecyclerAdapter
    private var carouselItemList: MutableList<CarouselItem> = mutableListOf()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val inflater = LayoutInflater.from(context)
        val carouselView: View = inflater.inflate(R.layout.carousel_view, this)

        infiniteViewPager = carouselView.findViewById(R.id.viewPager)

        val carouselItemList = mutableListOf<CarouselItem>()
        carouselItemList.add(CarouselItem("https://storage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg"))
        carouselItemList.add(CarouselItem("https://storage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg"))
        carouselItemList.add(CarouselItem("https://storage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg"))
        carouselItemList.add(CarouselItem("https://storage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerEscapes.jpg"))

        setupCarousel(carouselItemList)
        startAutoScroll()
    }

    private fun setupCarousel(carouselItemList: List<CarouselItem>) {
        this.carouselItemList.addAll(carouselItemList)

        infiniteRecyclerAdapter = InfiniteRecyclerAdapter(carouselItemList)
        infiniteViewPager.adapter = infiniteRecyclerAdapter

        // setting the current item of the infinite ViewPager to the actual first element
        infiniteViewPager.currentItem = 1

        // function for registering a callback to update the ViewPager
        // and provide a smooth flow for infinite scroll
        onInfinitePageChangeCallback(carouselItemList.size + 2)
    }

    private fun onInfinitePageChangeCallback(listSize: Int) {
        infiniteViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    when (infiniteViewPager.currentItem) {
                        listSize - 1 -> infiniteViewPager.setCurrentItem(1, false)
                        0 -> infiniteViewPager.setCurrentItem(listSize - 2, false)
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position != 0 && position != listSize - 1) {
                    // pageIndicatorView.setSelected(position-1)
                }
            }
        })
    }

    fun startAutoScroll() {
        // Start a coroutine for auto-scrolling
        coroutineScope.launch {
            while (true) {
                // Delay for a specified duration
                delay(3000L)

                // Switch to the main thread to update UI
                withContext(Dispatchers.Main) {
                    // Increment the current item to simulate auto-scrolling
                    infiniteViewPager.setCurrentItem(infiniteViewPager.currentItem + 1, true)
                }
            }
        }
    }

    fun stopAutoScroll() {
        coroutineScope.cancel()
    }
}
