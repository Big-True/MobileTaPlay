package com.mobiletaplay.app.input

/**
 * Keyboard injection protocol for mota-js (WebView).
 * Fixed `window.__MTP__` handshake so the page can detect the shell.
 */
object InjectJs {
    /** Call once per page load: exposes __MTP__ and input helpers. */
    val bridge: String = """
        (function(){
          if(window.__MTP__ && window.__MTP__.v >= 2) return;
          window.__MTP__ = {
            v: 2,
            ready: true,
            emit: function(keyCode, type){
              try{
                var e = new KeyboardEvent(type === 'up' ? 'keyup' : 'keydown', {bubbles:true, cancelable:true});
                Object.defineProperty(e, 'keyCode', {get:function(){return keyCode}});
                Object.defineProperty(e, 'which', {get:function(){return keyCode}});
                (document.activeElement || document.body).dispatchEvent(e);
              }catch(err){}
            }
          };
          if(!document.getElementById('mtp-style')){
            var st = document.createElement('style');
            st.id = 'mtp-style';
            st.textContent = 'html,body{background:#0f1419!important;color-scheme:dark}';
            document.head.appendChild(st);
          }
        })();
    """.trimIndent()

    /** Minimal per-event inject (down/up only). Falls back if __MTP__ missing. */
    fun key(keyCode: Int, up: Boolean): String {
        val type = if (up) "keyup" else "keydown"
        return """
            (function(){
              var kc=$keyCode;
              if(window.__MTP__ && window.__MTP__.emit){ window.__MTP__.emit(kc, '${if (up) "up" else "down"}'); return; }
              try{
                var e=new KeyboardEvent('$type',{bubbles:true,cancelable:true});
                Object.defineProperty(e,'keyCode',{get:function(){return kc}});
                Object.defineProperty(e,'which',{get:function(){return kc}});
                (document.activeElement||document.body||window).dispatchEvent(e);
                window.dispatchEvent(e);
              }catch(err){}
            })();
        """.trimIndent()
    }
}
