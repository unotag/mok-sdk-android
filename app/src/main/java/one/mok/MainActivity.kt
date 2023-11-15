package one.mok


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        val adapter = HomeViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(DashboardFragment(), "Dashboard")
        adapter.addFragment(SettingsFragment(), "Settings")

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)


        GlobalScope.launch {
            kotlinx.coroutines.delay(8000)
            launchEmptyActivity()
        }
    }

    fun launchEmptyActivity() {
        val intent = Intent(this, EmptyActivity::class.java)
        startActivity(intent)
    }
}
