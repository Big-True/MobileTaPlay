package com.mobiletaplay.app.input

/**
 * mota-js landscape needs body.clientWidth >= 551.
 * Dual-screen handheld secondary panel is 1240x1080 → ~538 CSS px, so the engine
 * always picks isVertical (portrait column, pinned to the top) even when the
 * window is landscape. Force landscape metrics whenever window width > height.
 */
object CoverFitJs {
    val install = """
        (function(){
          if (window.__MTP_FIT__) { return 'ok'; }

          try {
            if (screen && screen.orientation) {
              screen.orientation.lock = function(){
                try { return Promise.resolve(); } catch (e) { return { then: function(){} }; }
              };
              screen.orientation.unlock = function(){};
            }
          } catch (e) {}

          function vw() { return window.innerWidth || (document.documentElement && document.documentElement.clientWidth) || 0; }
          function vh() { return window.innerHeight || (document.documentElement && document.documentElement.clientHeight) || 0; }

          // Landscape window → pretend body is just wide enough for mota-js landscape.
          // Threshold: clientWidth - 9 >= 416+129 → clientWidth >= 554.
          // Use 554 (not 560) so centering error stays minimal; recenterGameGroup()
          // then snaps #gameGroup to the real viewport.
          function withForcedWidth(fn) {
            var body = document.body;
            if (!body) return fn();
            var W = vw(), H = vh();
            if (!(W > H)) return fn();
            var forced = Math.max(554, W);
            var ok = false;
            try {
              Object.defineProperty(body, 'clientWidth', {
                configurable: true,
                get: function() { return forced; }
              });
              ok = true;
            } catch (e) {}
            try {
              return fn();
            } finally {
              if (ok) {
                try { delete body.clientWidth; } catch (e) {}
              }
            }
          }

          /** Engine centers with the fake width — snap back to the real viewport. */
          function recenterGameGroup() {
            try {
              var g = document.getElementById('gameGroup');
              if (!g || !g.style) return;
              var W = vw(), H = vh();
              if (!W || !H) return;
              var r = g.getBoundingClientRect();
              if (r.width < 2 || r.height < 2) return;
              var left = parseFloat(g.style.left);
              var top = parseFloat(g.style.top);
              if (isNaN(left)) left = 0;
              if (isNaN(top)) top = 0;
              // Desired top-left so the box is centered in the real window.
              var wantLeft = (W - r.width) / 2;
              var wantTop = (H - r.height) / 2;
              var dx = wantLeft - r.left;
              var dy = wantTop - r.top;
              if (Math.abs(dx) > 0.5 || Math.abs(dy) > 0.5) {
                g.style.left = (left + dx) + 'px';
                g.style.top = (top + dy) + 'px';
              }
            } catch (e) {}
          }

          function pick() {
            var g = document.getElementById('gameGroup');
            if (g && g.getBoundingClientRect().width > 4) return g;
            var c = document.getElementById('gameCanvas') || document.querySelector('canvas.gameCanvas') || document.querySelector('canvas');
            if (c && c.getBoundingClientRect().width > 4) return c;
            return null;
          }

          function clearOurs(el) {
            if (el && el.getAttribute && el.getAttribute('data-mtp-fit') === '1') {
              el.style.transform = '';
              el.style.transformOrigin = '';
              el.removeAttribute('data-mtp-fit');
            }
          }

          // Patch engine resize itself (load-save / floor-change call core.resize
          // directly and would bypass window.onresize).
          function wrapResize(fn) {
            if (typeof fn !== 'function' || fn.__mtp_wrapped) return fn;
            var wrapped = function() {
              var self = this, args = arguments;
              return withForcedWidth(function() {
                var r = fn.apply(self, args);
                try {
                  if (window.core && core.domStyle && core.domStyle.isVertical && vw() > vh()) {
                    core.domStyle.isVertical = false;
                    fn.apply(self, args);
                  }
                } catch (e) {}
                recenterGameGroup();
                return r;
              });
            };
            wrapped.__mtp_wrapped = true;
            return wrapped;
          }

          function patchEngineResize() {
            try {
              if (!window.core) return false;
              if (core.resize) core.resize = wrapResize(core.resize);
              if (core.control && core.control.resize) {
                core.control.resize = wrapResize(core.control.resize);
              }
              return true;
            } catch (e) { return false; }
          }

          function engineResize() {
            patchEngineResize();
            withForcedWidth(function() {
              try {
                if (window.core && typeof core.resize === 'function') core.resize();
              } catch (e) {}
              try {
                if (window.core && core.domStyle && core.domStyle.isVertical && vw() > vh()) {
                  core.domStyle.isVertical = false;
                  if (typeof core.resize === 'function') core.resize();
                }
              } catch (e) {}
            });
            recenterGameGroup();
          }

          function scheduleEngine() {
            clearTimeout(window.__MTP_ERS);
            window.__MTP_ERS = setTimeout(engineResize, 120);
          }

          function apply(mode) {
            engineResize();
            var el = pick();
            if (!el) return 'no-target';
            clearOurs(el);

            var W = vw(), H = vh();
            var r = el.getBoundingClientRect();
            if (r.width < 2 || r.height < 2 || !W || !H) return 'no-size';

            var overflows = r.width > W * 1.01 || r.height > H * 1.01;
            if (!overflows) {
              var tiny = r.width < W * 0.72 || r.height < H * 0.55;
              var high = r.top < H * 0.22 && r.bottom < H * 0.88;
              if (tiny && high) {
                scheduleEngine();
                return 'repair';
              }
              return 'fits';
            }

            var s = Math.min(W / r.width, H / r.height, 1);
            if (!(s > 0.05 && s < 1)) return 'bad:' + s;
            var tx = (W - r.width * s) / 2;
            var ty = (H - r.height * s) / 2;
            el.style.transformOrigin = '0 0';
            el.style.transform = 'translate(' + tx + 'px,' + ty + 'px) scale(' + s + ')';
            el.setAttribute('data-mtp-fit', '1');
            return 'shrink:' + s.toFixed(3);
          }

          function armResizeGuard() {
            var tries = 0;
            var iv = setInterval(function() {
              tries++;
              patchEngineResize();
              if (window.core && typeof core.resize === 'function' && core.resize.__mtp_wrapped) {
                clearInterval(iv);
                window.onresize = function() {
                  clearTimeout(window.__MTP_ERS);
                  window.__MTP_ERS = setTimeout(function(){
                    engineResize();
                    apply();
                  }, 160);
                };
                engineResize();
                apply();
              } else if (tries > 50) {
                clearInterval(iv);
              }
            }, 180);
          }

          window.__MTP_FIT__ = {
            apply: apply,
            nudge: function(){
              scheduleEngine();
              return apply();
            },
            engineResize: engineResize
          };

          window.addEventListener('resize', function(){
            clearTimeout(window.__MTP_FIT_T);
            window.__MTP_FIT_T = setTimeout(function(){
              engineResize();
              apply();
            }, 160);
          });

          armResizeGuard();
          apply();
          return 'ok';
        })();
    """.trimIndent()

    fun apply(mode: String): String =
        "(function(){try{return window.__MTP_FIT__ ? window.__MTP_FIT__.nudge() : 'not-installed';}catch(e){return 'err';}})();"
}
