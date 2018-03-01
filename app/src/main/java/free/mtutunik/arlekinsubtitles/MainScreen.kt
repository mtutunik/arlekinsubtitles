package free.mtutunik.arlekinsubtitles

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main_screen.*
import android.view.inputmethod.InputMethodManager
import android.preference.PreferenceManager
import android.os.PowerManager
import android.webkit.WebViewClient


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MainScreen : AppCompatActivity() {
    //private  lateinit var settingsBtn : ImageButton;
    private var mSubtitlesUrl = "";
    private var mWakeLock: PowerManager.WakeLock? = null
    private lateinit var ctx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = this
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        mSubtitlesUrl = settings.getString("subtitlesurl", "")

        setContentView(R.layout.activity_main_screen)
        subtitlesView.webViewClient = WebViewClient()

        sourceUrl.setOnEditorActionListener(object: TextView.OnEditorActionListener {
            override fun onEditorAction(editView: TextView?, actionId: Int, evt: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    mSubtitlesUrl = editView?.text.toString()
                    sourceUrl.visibility = View.GONE
                    channelName.visibility = sourceUrl.visibility
                    settingsBtn.visibility = View.VISIBLE
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(sourceUrl.getWindowToken(), 0)

                    val settings = PreferenceManager.getDefaultSharedPreferences(ctx)
                    val editor = settings.edit()
                    editor.putString("subtitlesurl", mSubtitlesUrl)
                    editor.commit()
                    subtitlesView.loadUrl(SUBTITLES_SERVER_URL + "/" + mSubtitlesUrl)
                }
                return true
            }
        })

        sourceUrl.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(view: View?, isFocus: Boolean) {
                if (!isFocus) {
                    sourceUrl.onEditorAction(EditorInfo.IME_ACTION_SEND)
                }
            }
        })

        sourceUrl.visibility = View.GONE
        settingsBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                sourceUrl.visibility = View.VISIBLE
                channelName.visibility = sourceUrl.visibility
                sourceUrl.setText(mSubtitlesUrl)
                settingsBtn.visibility = View.GONE
                sourceUrl.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(sourceUrl, InputMethodManager.SHOW_IMPLICIT)
            }
        })


        val pm = this.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SubtitlesViewPowerLock")
        mWakeLock?.setReferenceCounted(false)
        if (!mWakeLock?.isHeld!!) {
            mWakeLock?.acquire()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mWakeLock?.release()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        subtitlesView.loadUrl(SUBTITLES_SERVER_URL + "/" + mSubtitlesUrl)
    }


    companion object {
        private val SUBTITLES_SERVER_URL = "https://s.subtitlesfortheatre.com"

        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }
}
