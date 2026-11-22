(function () {
    'use strict';

    var chart = document.getElementById('chart');
    if (!chart) {
        return;
    }

    var FONT = 'system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif';

    // ---------- Imagen descargable (se dibuja en el navegador; no se envía nada al servidor)

    function readRows() {
        return Array.prototype.map.call(chart.querySelectorAll('.axis-row'), function (row) {
            var badges = row.querySelectorAll('.pole-badge');
            return {
                leftLabel: row.dataset.leftLabel,
                rightLabel: row.dataset.rightLabel,
                leftPct: parseInt(row.dataset.leftPct, 10),
                rightPct: parseInt(row.dataset.rightPct, 10),
                leftColor: getComputedStyle(row.querySelector('.seg-left')).backgroundColor,
                rightColor: getComputedStyle(row.querySelector('.seg-right')).backgroundColor,
                leftIcon: badges[0] ? badges[0].querySelector('svg') : null,
                rightIcon: badges[1] ? badges[1].querySelector('svg') : null
            };
        });
    }

    // Convierte un <svg> de la página en una imagen que se puede pintar en el canvas.
    function svgToImage(svg) {
        return new Promise(function (resolve) {
            if (!svg) {
                resolve(null);
                return;
            }
            var xml = new XMLSerializer().serializeToString(svg);
            var img = new Image();
            img.onload = function () { resolve(img); };
            img.onerror = function () { resolve(null); };
            img.src = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(xml);
        });
    }

    function roundedRectPath(ctx, x, y, w, h, r) {
        ctx.beginPath();
        ctx.moveTo(x + r, y);
        ctx.lineTo(x + w - r, y);
        ctx.quadraticCurveTo(x + w, y, x + w, y + r);
        ctx.lineTo(x + w, y + h - r);
        ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
        ctx.lineTo(x + r, y + h);
        ctx.quadraticCurveTo(x, y + h, x, y + h - r);
        ctx.lineTo(x, y + r);
        ctx.quadraticCurveTo(x, y, x + r, y);
        ctx.closePath();
    }

    // Reduce el tamaño de letra hasta que las dos etiquetas quepan en la misma línea.
    function fitFontSize(ctx, leftText, rightText, maxWidth, size) {
        var s = size;
        while (s > 16) {
            ctx.font = '600 ' + s + 'px ' + FONT;
            if (ctx.measureText(leftText).width + ctx.measureText(rightText).width + 40 <= maxWidth) {
                break;
            }
            s -= 1;
        }
        return s;
    }

    // Parte un texto en líneas que quepan en maxWidth (con la fuente ya configurada en ctx).
    function wrapText(ctx, text, maxWidth) {
        var words = text.split(/\s+/);
        var lines = [];
        var line = '';
        words.forEach(function (word) {
            var test = line ? line + ' ' + word : word;
            if (line && ctx.measureText(test).width > maxWidth) {
                lines.push(line);
                line = word;
            } else {
                line = test;
            }
        });
        if (line) {
            lines.push(line);
        }
        return lines;
    }

    // Logo del polo: círculo de color con el icono encima.
    function drawBadge(ctx, color, icon, cx, cy, radius) {
        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.arc(cx, cy, radius, 0, Math.PI * 2);
        ctx.fill();
        if (icon) {
            var size = radius * 1.36;
            ctx.drawImage(icon, cx - size / 2, cy - size / 2, size, size);
        }
    }

    function drawImage(rows, flagImg, icons) {
        var slogan = (document.getElementById('slogan') || {}).textContent || '';
        var W = 1080, M = 60, ROW = 124, BOTTOM = 110;
        var hasFlag = !!flagImg;
        var TOP = hasFlag ? 450 : 190;
        var H = TOP + rows.length * ROW + BOTTOM;
        var barW = W - 2 * M, barH = 52, R = 20, GAP = 12;

        var canvas = document.createElement('canvas');
        canvas.width = W;
        canvas.height = H;
        var ctx = canvas.getContext('2d');

        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, W, H);

        ctx.fillStyle = '#1d1f24';
        ctx.textAlign = 'left';
        ctx.textBaseline = 'alphabetic';
        ctx.font = '700 52px ' + FONT;
        ctx.fillText(chart.dataset.title || '', M, 100);

        if (hasFlag) {
            var fw = 390, fh = 260, fx = M, fy = 150;
            ctx.save();
            ctx.shadowColor = 'rgba(0,0,0,0.28)';
            ctx.shadowBlur = 22;
            ctx.shadowOffsetY = 8;
            ctx.drawImage(flagImg, fx, fy, fw, fh);
            ctx.restore();

            var textX = fx + fw + 50, textW = W - M - textX, size = 54, lines;
            do {
                ctx.font = '800 ' + size + 'px ' + FONT;
                lines = wrapText(ctx, slogan, textW);
                size -= 4;
            } while (lines.length > 3 && size > 28);
            var lh = Math.round((size + 4) * 1.25);
            var startY = fy + fh / 2 - ((lines.length - 1) * lh) / 2;
            ctx.fillStyle = '#1d1f24';
            ctx.textAlign = 'left';
            ctx.textBaseline = 'middle';
            lines.forEach(function (l, i) {
                ctx.fillText(l, textX, startY + i * lh);
            });
        }

        rows.forEach(function (r, i) {
            var y = TOP + i * ROW;
            var size = fitFontSize(ctx, r.leftLabel, r.rightLabel, W - 2 * M - 2 * (2 * R + GAP), 28);

            drawBadge(ctx, r.leftColor, icons[i].left, M + R, y + R + 2, R);
            drawBadge(ctx, r.rightColor, icons[i].right, W - M - R, y + R + 2, R);

            ctx.font = '600 ' + size + 'px ' + FONT;
            ctx.fillStyle = '#1d1f24';
            ctx.textBaseline = 'middle';
            ctx.textAlign = 'left';
            ctx.fillText(r.leftLabel, M + 2 * R + GAP, y + R + 2);
            ctx.textAlign = 'right';
            ctx.fillText(r.rightLabel, W - M - 2 * R - GAP, y + R + 2);

            var barY = y + 52;
            var leftW = Math.round(barW * r.leftPct / 100);

            ctx.save();
            roundedRectPath(ctx, M, barY, barW, barH, 12);
            ctx.clip();
            ctx.fillStyle = r.leftColor;
            ctx.fillRect(M, barY, leftW, barH);
            ctx.fillStyle = r.rightColor;
            ctx.fillRect(M + leftW, barY, barW - leftW, barH);
            ctx.restore();

            ctx.fillStyle = '#ffffff';
            ctx.font = '700 28px ' + FONT;
            ctx.textBaseline = 'middle';
            if (leftW > 80) {
                ctx.textAlign = 'right';
                ctx.fillText(r.leftPct + '%', M + leftW - 14, barY + barH / 2);
            }
            if (barW - leftW > 80) {
                ctx.textAlign = 'left';
                ctx.fillText(r.rightPct + '%', M + leftW + 14, barY + barH / 2);
            }
        });

        ctx.fillStyle = '#5d6370';
        ctx.font = '400 28px ' + FONT;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'alphabetic';
        ctx.fillText(window.location.host, W / 2, H - 44);

        return canvas;
    }

    function downloadImage() {
        var rows = readRows();
        var flagSvg = document.getElementById('flag');
        var jobs = [svgToImage(flagSvg)];
        rows.forEach(function (r) {
            jobs.push(svgToImage(r.leftIcon), svgToImage(r.rightIcon));
        });
        Promise.all(jobs).then(function (images) {
            var icons = rows.map(function (r, i) {
                return { left: images[1 + 2 * i], right: images[2 + 2 * i] };
            });
            var canvas = drawImage(rows, images[0], icons);
            canvas.toBlob(function (blob) {
                if (!blob) {
                    return;
                }
                var url = URL.createObjectURL(blob);
                var a = document.createElement('a');
                a.href = url;
                a.download = (chart.dataset.filename || 'result') + '.png';
                document.body.appendChild(a);
                a.click();
                a.remove();
                setTimeout(function () { URL.revokeObjectURL(url); }, 1000);
            }, 'image/png');
        });
    }

    // ---------- Enlace (solo en la página pública del resultado)

    var urlInput = document.getElementById('share-url');
    if (urlInput) {
        urlInput.value = window.location.href;
    }

    function flash(button, text) {
        var original = button.textContent;
        button.textContent = text;
        setTimeout(function () { button.textContent = original; }, 1800);
    }

    function copyLink(button) {
        var done = function () { flash(button, button.dataset.done || 'OK'); };
        var fallback = function () {
            if (urlInput) {
                urlInput.select();
                document.execCommand('copy');
                done();
            }
        };
        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(window.location.href).then(done, fallback);
        } else {
            fallback();
        }
    }

    function nativeShare() {
        navigator.share({ title: chart.dataset.title || document.title, url: window.location.href })
            .catch(function () { /* la persona canceló */ });
    }

    // El botón "Compartir" del sistema solo se muestra si el navegador lo soporta (móviles).
    var shareButton = document.querySelector('[data-action="share"]');
    if (shareButton && navigator.share) {
        shareButton.hidden = false;
    }

    document.addEventListener('click', function (event) {
        var button = event.target.closest('[data-action]');
        if (!button) {
            return;
        }
        var action = button.dataset.action;
        if (action === 'download') {
            downloadImage();
        } else if (action === 'copy') {
            copyLink(button);
        } else if (action === 'share') {
            nativeShare();
        }
    });
})();
