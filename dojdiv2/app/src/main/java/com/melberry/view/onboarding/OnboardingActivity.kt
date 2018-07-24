package com.melberry.view.onboarding

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.melberry.R
import com.melberry.utils.City
import com.melberry.view.MainActivity

class OnboardingActivity : AppCompatActivity(), FeaturesFragment.OnFeaturesFragmentInteractionListener, DisabilityFragment.OnDisabilityFragmentInteractionListener, CitiesFragment.OnCitiesFragmentInteractionListener {

    private val featuresFragment = FeaturesFragment()
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Analytics
        analytics = FirebaseAnalytics.getInstance(application)

        // region Dynamic Links region
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
                .addOnSuccessListener {
                    OnSuccessListener<PendingDynamicLinkData> {
                        // Analytics
                        val params = Bundle()
                        params.putString(getString(R.string.event_key), getString(R.string.onSuccessDynamicLink_key))
                        analytics.logEvent(getString(R.string.onSuccessDynamicLink_key), params)
                    }
                }.addOnFailureListener {
                    OnFailureListener {
                        // Analytics
                        val params = Bundle()
                        params.putString(getString(R.string.event_key), getString(R.string.onFailureDynamicLink_key))
                        analytics.logEvent(getString(R.string.onFailureDynamicLink_key), params)
                    }
                }
        // endregion

        // region Launching animation + opening another onboarding or cities menu region
        val layout = findViewById<FrameLayout>(R.id.onboarding_container)
        val logo = findViewById<ImageView>(R.id.logo)
        logo.alpha = 0f

        val extras: Bundle? = intent.extras
        if (extras?.getBoolean(getString(R.string.skip_animation_key)) == true) {
            openCitiesFragment()
        } else {
            layout.viewTreeObserver.addOnPreDrawListener(
                    object : ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            layout.viewTreeObserver.removeOnPreDrawListener(this)

                            val darkBlueImageView = findViewById<ImageView>(R.id.darkblue_animation_part)
                            darkBlueImageView.translationY = layout.height.toFloat()
                            darkBlueImageView.animate()
                                    .translationY(0 - darkBlueImageView.height.toFloat() / 1.5f)
                                    .setDuration(1000)
                                    .setListener(null)


                            val tirkisBlueImageView = findViewById<ImageView>(R.id.tirkis_animation_part)
                            tirkisBlueImageView.translationX = 0 - tirkisBlueImageView.height.toFloat()
                            tirkisBlueImageView.translationY = layout.height - layout.height / 4 - 0f
                            tirkisBlueImageView.animate()
                                    .translationX(layout.width.toFloat() - tirkisBlueImageView.width / 2)
                                    .translationY(0f)
                                    .setDuration(1000)
                                    .setListener(null)

                            val lightBlueImageView = findViewById<ImageView>(R.id.lightblue_animation_part)
                            lightBlueImageView.translationX = layout.width.toFloat()
                            lightBlueImageView.translationY = layout.height.toFloat()
                            lightBlueImageView.animate()
                                    .translationX(0f - lightBlueImageView.width / 2)
                                    .translationY(layout.height - layout.height / 3 + 0f)
                                    .setDuration(1000)
                                    .setListener(
                                            object : Animator.AnimatorListener {
                                                override fun onAnimationRepeat(animation: Animator?) {}

                                                override fun onAnimationCancel(animation: Animator?) {}

                                                override fun onAnimationStart(animation: Animator?) {}

                                                override fun onAnimationEnd(animation: Animator?) {
                                                    logo.animate().alpha(1f)
                                                }
                                            })
                            return true
                        }
                    })

            object : CountDownTimer(2000, 1000) {
                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {
                    if (isMainActivityOpenedAtLaunch()) openCitiesFragment()
                    else openFeaturesFragment()

                }
            }.start()
        }

        // endregion
    }

    // Listener from FeaturesFragment to open next (disability) fragment
    override fun onFeaturesFragmentInteraction() {
        openDisabilityFragment()
    }

    // Listener from DisabilityFragment to open next (Cities menu) fragment
    override fun onDisabilityFragmentInteraction() {
        openCitiesFragment()
    }

    // Open Main Activity
    override fun openActivityFromSelectedCity(city: City?) {
        // Save that onboarding has been shown and main activity should be open after launch
        savePreferences(true)

        // Save which city should be opend
        saveCityPreferences(city)

        // Open MainActivity and finish this activity
        val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Open mail client
    override fun openMailClient() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "plain/text"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.contact_email)))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_email_subject))
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.contact_email_txt))
        startActivity(Intent.createChooser(intent, ""))
    }

    // region Manage fragments region
    private fun openFeaturesFragment() {
        replaceFragment(featuresFragment, R.id.onboarding_container)
    }

    private fun openDisabilityFragment() {
        val disabilityFragment = DisabilityFragment()
        replaceFragment(disabilityFragment, R.id.onboarding_container)
    }

    private fun openCitiesFragment() {
        val citiesFragment = CitiesFragment()
        replaceFragment(citiesFragment, R.id.onboarding_container)
    }
    // endregion

    override fun onBackPressed() {
        // If FeaturesFragment is visible, onBackPress changes the features in the fragment
        if (!featuresFragment.isVisible) super.onBackPressed()
    }

    // region Save preferences region
    private fun savePreferences(isMainActivityOpenedAtLaunch: Boolean) {
        val sharedPreferences: SharedPreferences = this.application.getSharedPreferences(getString(R.string.is_main_acitivty_opend_key), Context.MODE_PRIVATE)
                ?: return

        with(sharedPreferences.edit()) {
            putBoolean(getString(R.string.is_main_acitivty_opend_key), isMainActivityOpenedAtLaunch)
            apply()
        }
    }

    private fun saveCityPreferences(city: City?) {
        val sharedPreferences: SharedPreferences = this.application.getSharedPreferences(getString(R.string.city_preference_key), Context.MODE_PRIVATE)
                ?: return

        with(sharedPreferences.edit()) {
            putString(getString(R.string.city_preference_key), city?.name)
            apply()
        }
    }

    private fun isMainActivityOpenedAtLaunch(): Boolean {
        val sharedPref = this.application.getSharedPreferences(getString(R.string.is_main_acitivty_opend_key), Context.MODE_PRIVATE)
        return sharedPref.getBoolean(getString(R.string.is_main_acitivty_opend_key), false)
    }
    // endregion

    // region Fragment managing
    private inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
        beginTransaction().func().commitAllowingStateLoss()
    }

    private fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) {
        supportFragmentManager.inTransaction { replace(frameId, fragment).addToBackStack(fragment.javaClass.simpleName) }
    }
    // endregion

}
