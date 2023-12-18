package com.example.thefesta.bottomnavi

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.thefesta.R
import okhttp3.Cookie


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Scheduler.newInstance] factory method to
 * create an instance of this fragment.
 */
class Scheduler : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_scheduler, container, false);

        val webView: WebView = view.findViewById<WebView>(R.id.webView)

        webView.settings.run {
            javaScriptEnabled = true;
            javaScriptCanOpenWindowsAutomatically = true;
        }

        //브러우저를 가지고 옴
        webView.webViewClient = CookWebViewClient();
        webView.webChromeClient = CookWebChromeClient();
        //웹 셋팅을 함
        val webSet = webView.settings;

        //크고 작게하는 기능을 줌
        webSet!!.setBuiltInZoomControls(true);
        webSet!!.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
        webSet!!.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        webSet!!.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        webSet!!.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        webSet!!.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        webSet!!.setSupportZoom(false); // 화면 줌 허용 여부
        webSet!!.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        webSet!!.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        webSet!!.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        webSet!!.setDomStorageEnabled(true); // 로컬저장소 허용 여부
        webView!!.webViewClient = WebViewClient()
        webView!!.loadUrl("http://192.168.4.15:9090/Scheduler/")
        webView!!.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (webView != null) {
                        if (webView.canGoBack()) {
                            webView.goBack()
                        } else {
                            requireActivity().onBackPressed()
                        }
                    }
                }
            }
            true
        }

        return view;
    }

    class CookWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return super.shouldOverrideUrlLoading(view, url)
        }

    }

    class CookWebChromeClient : WebChromeClient(){

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            Log.d("ConsoleLog", consoleMessage?.message() + '\n' + consoleMessage?.messageLevel() + '\n' + consoleMessage?.sourceId());
            return super.onConsoleMessage(consoleMessage)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Scheduler.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Scheduler().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}