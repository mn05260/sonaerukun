    // --- Firebase設定 ---
    const firebaseConfig = {
        apiKey: "AIzaSyA1MU0XeeE1CxUP2dg-qeqBk2zOSHeph6-hg",
        authDomain: "sonaerukun-0526.firebaseapp.com",
        projectId: "sonaerukun-0526",
        storageBucket: "sonaerukun-0526.firebasestorage.app",
        messagingSenderId: "134983842372",
        appId: "1:134983842372:web:f620291243b74e33c31a11",
        databaseURL: "https://sonaerukun-0526-default-rtdb.asia-southeast1.firebasedatabase.app"
    };

    firebase.initializeApp(firebaseConfig);
    const database = firebase.database();
    let currentKeyword = "";

    // タブ切り替え
    function switchTab(tabName) {
        document.getElementById('calc-content').style.display = (tabName === 'calc') ? 'block' : 'none';
        document.getElementById('manage-content').style.display = (tabName === 'manage') ? 'block' : 'none';
        document.getElementById('btn-calc').classList.toggle('active', tabName === 'calc');
        document.getElementById('btn-manage').classList.toggle('active', tabName === 'manage');
    }

    // 生理用品エリアの表示切り替え
    function toggleHygieneArea() {
        const count = document.getElementById('femaleCount').value;
        const area = document.getElementById('hygiene-area');
        if(area) area.style.display = (count > 0) ? 'block' : 'none';
    }

    // ランクCの表示切り替え
    function toggleRankC() {
        const content = document.getElementById('rank-c-content');
        const btn = document.getElementById('toggle-rank-c');
        const isHidden = content.style.display === 'none';
        content.style.display = isHidden ? 'grid' : 'none';
        btn.innerText = isHidden ? '閉じる' : '表示';
    }

    // Amazon検索
    function searchItem(itemName) {
        const cleanName = itemName.split(':')[0].trim();
        window.open(`https://www.amazon.co.jp/s?k=${encodeURIComponent(cleanName)} 防災`, '_blank');
    }

    // アイテムを追加する
    function addItem() {
    const nameInput = document.getElementById('new-item-name');
    const dateInput = document.getElementById('new-item-date');
    const name = nameInput.value.trim();
    const date = dateInput.value;
    if (!name) return alert("アイテム名を入力してください");

    createRow(name, date); // 下の新しいcreateRowが動きます
    sortItemsByDate(); 
    nameInput.value = '';
    dateInput.value = '';
    saveAllData();
}

// 2. カードUI生成関数（重要：ここをtable用からdiv用に変更）
function createRow(name, date) {
    const container = document.getElementById('expiry-list-container');
    if (!container) return;

    const card = document.createElement('div');
    card.className = 'item-card';
    
    card.innerHTML = `
        <div class="card-info">
            <input type="text" class="card-item-name" value="${name}" onchange="saveAllData()" placeholder="アイテム名">
            <div style="display: flex; align-items: center; gap: 5px;">
                <span style="font-size: 0.75rem; color: #94a3b8;">期限:</span>
                <input type="month" class="card-item-date" value="${date}" 
                       onchange="saveAllData(); updateCardColor(this.parentElement.parentElement.parentElement); sortItemsByDate();">
            </div>
        </div>
        <button type="button" class="card-delete-btn" onclick="this.parentElement.remove(); saveAllData();">×</button>
    `;
    container.appendChild(card);
    updateCardColor(card);
}

// 3. カードの色判定関数（クラス名をカード用に変更）
function updateCardColor(card) {
    const dateInput = card.querySelector('.card-item-date');
    if (!dateInput || !dateInput.value) return;

    const expiryDate = new Date(dateInput.value + "-01");
    const today = new Date();
    const currentMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const diffMonths = (expiryDate.getFullYear() - currentMonth.getFullYear()) * 12 + (expiryDate.getMonth() - currentMonth.getMonth());

    card.classList.remove('status-danger-card', 'status-warning-card', 'status-safe-card');

    if (diffMonths < 1) {
        card.classList.add('status-danger-card');
    } else if (diffMonths < 3) {
        card.classList.add('status-warning-card');
    } else {
        card.classList.add('status-safe-card');
    }
}

// 4. 保存関数（取得先のクラス名をカード用に変更）
function saveAllData() {
    const names = document.querySelectorAll('.card-item-name');
    const dates = document.querySelectorAll('.card-item-date');
    const items = Array.from(names).map((el, i) => ({ 
        name: el.value, 
        date: dates[i].value 
    })).filter(item => item.name !== "");

    const memo = {
        place: document.getElementById('evac-place').value,
        rule: document.getElementById('family-rule').value,
        tel: document.getElementById('emergency-tel').value
    };

    const allData = { items, memo };
    localStorage.setItem('sonaerukun_v3_data', JSON.stringify(allData));
    if (currentKeyword) {
        database.ref('users/' + currentKeyword).set(allData);
    }
}
   function startSync() {
    const keyword = document.getElementById('sync-keyword').value.trim();
    if (!keyword) return alert("合言葉を入力してください");

    currentKeyword = keyword;
    localStorage.setItem('sonaerukun_keyword', keyword);

    // 1. まずはJava（RenderのDB）に合言葉を保存しに行く
    fetch('/joinFamily', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'keyword=' + encodeURIComponent(keyword)
    })
    .then(response => {
        // 2. DBの保存が終わったら、Firebaseからデータを取ってくる
        return database.ref('users/' + keyword).once('value');
    })
    .then((snapshot) => {
        if (snapshot.exists()) {
            reflectDataToUI(snapshot.val());
            alert("家族のデータと同期しました！");
        } else {
            saveAllData();
            alert("新しいグループを作成しました！");
        }
        observeData();
    })
    .catch(error => {
        console.error("同期中にエラーが発生しました:", error);
        alert("同期に失敗しました。ネット接続を確認してください。");
    });
}

    function observeData() {
        if (!currentKeyword) return;
        database.ref('users/' + currentKeyword).on('value', (snapshot) => {
            if (snapshot.exists()) reflectDataToUI(snapshot.val());
        });
    }

    // 取得したデータを画面に反映
   function reflectDataToUI(data) {
    const container = document.getElementById('expiry-list-container');
    if (!container) return;
    if (data.items) {
        container.innerHTML = ''; 
        data.items.forEach(item => createRow(item.name, item.date));
    }
    if (data.memo) {
        document.getElementById('evac-place').value = data.memo.place || '';
        document.getElementById('family-rule').value = data.memo.rule || '';
        document.getElementById('emergency-tel').value = data.memo.tel || '';
    }
}
window.onload = function() {
    // ローカルのデータを表示
    const local = JSON.parse(localStorage.getItem('sonaerukun_v3_data') || '{}');
    if (local.items || local.memo) {
        reflectDataToUI(local);
    }
    const syncInput = document.getElementById('sync-keyword');
    const familyHostName = syncInput ? syncInput.value : ''; 
    if (familyHostName && familyHostName !== '' && !familyHostName.includes('[[')) { 
        currentKeyword = familyHostName;
        localStorage.setItem('sonaerukun_keyword', familyHostName);
        
        observeData(); 
        console.log("DBの合言葉で同期開始: " + familyHostName);
    } else {
        const savedKeyword = localStorage.getItem('sonaerukun_keyword');
        if (savedKeyword) {
            if (syncInput) syncInput.value = savedKeyword;
            currentKeyword = savedKeyword;
            observeData();
            console.log("保存済みの合言葉で同期開始: " + savedKeyword);
        }
    }

    updateTotalCount();
    checkAllInputs();
    sortItemsByDate();
};
    // 入力制限
    function limitInput(input) {
        input.style.borderColor = "#e2e8f0";
        if (input.value === "") {
            input.style.borderColor = "#ef4444";
        } else {
            if (parseFloat(input.value) > 100) {
                alert("100人以内で入力してくださいね！");
                input.value = 100;
            } else if (parseFloat(input.value) < 0) {
                input.value = 0;
            }
        }
        checkAllInputs();
    }

    function checkAllInputs() {
        const inputs = document.querySelectorAll('input[type="number"]');
        const submitBtn = document.getElementById('submit-btn');
        const errorMsg = document.getElementById('error-message');
        let hasError = false;
        inputs.forEach(input => {
            if (input.value === "" || isNaN(input.value)) hasError = true;
        });

        if (hasError) {
            submitBtn.disabled = true;
            submitBtn.style.opacity = "0.5";
            submitBtn.style.cursor = "not-allowed";
            if(errorMsg) errorMsg.style.display = "block";
        } else {
            submitBtn.disabled = false;
            submitBtn.style.opacity = "1";
            submitBtn.style.cursor = "pointer";
            if(errorMsg) errorMsg.style.display = "none";
        }
    }

    function validateAll() {
        const inputs = document.querySelectorAll('input[type="number"]');
        for (let input of inputs) {
            if (input.value === "") {
                alert("未入力の項目があります。");
                input.focus();
                return false; 
            }
        }
        return true; 
    }

    function updateTotalCount() {
        const male = parseInt(document.getElementById('maleCount').value) || 0;
        const female = parseInt(document.getElementById('femaleCount').value) || 0;
        const child = parseInt(document.getElementById('childCount').value) || 0;
        const infant = parseInt(document.getElementsByName('infantCount')[0].value) || 0;
        const senior = parseInt(document.getElementsByName('seniorCount')[0].value) || 0;
        document.getElementById('familyCount').value = male + female + child + infant + senior;
        checkAllInputs();
    }
    function sortItemsByDate() {
    const container = document.getElementById('expiry-list-container');
    if (!container) return;
    const cards = Array.from(container.querySelectorAll('.item-card'));
    cards.sort((a, b) => {
        const dateA = a.querySelector('.card-item-date').value;
        const dateB = b.querySelector('.card-item-date').value;
        if (!dateA) return 1;
        if (!dateB) return -1;
        return new Date(dateA) - new Date(dateB);
    });
    cards.forEach(card => container.appendChild(card));
}
function showRank(rankName, e) {
    // 1. すべてのランクコンテンツを隠す
    document.querySelectorAll('.rank-content').forEach(el => el.style.display = 'none');
    
    // 2. すべてのボタンの active クラスを消す
    document.querySelectorAll('.rank-tab').forEach(btn => btn.classList.remove('active'));
    
    // 3. 指定されたランクを表示
    const target = document.getElementById('content-' + rankName);
    if (target) {
        target.style.display = 'block';
    }
    
    // 4. クリックされたボタンに active をつける
    // event.currentTarget の代わりに e.currentTarget を使います
    if (e && e.currentTarget) {
        e.currentTarget.classList.add('active');
    }
}
function showSyncQR(){
    const keyword = document.getElementById('sync-keyword').value;
    if(!keyword) return alert("合言葉を入力してください");
    //QRコードを表示する場所を指定する
    const area = document.getElementById('qrcode-area');
    if(area) area.style.display = 'flex';
    const qrContainer = document.getElementById("qrcode");
    qrContainer.innerHTML = "";
    //QRコードを作成
    new QRCode(qrContainer, {
        text: "http://localhost:8081/signup?hostName=" + keyword,
        width:160,
        height:160,
        colorDark:"#2d5a27",
        colorLight:"#ffffff",
        correctLevel: QRCode.CorrectLevel.H
    });
}
let videoStream = null;

async function startCamera() {
    const video = document.getElementById("video");
    const cameraSection = document.getElementById("camera-section");
    cameraSection.style.display = "block";

    // 1. カメラを起動
    try {
        videoStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: "environment" } });
        video.srcObject = videoStream;
        video.setAttribute("playsinline", true); // iOS対策
        await video.play();
        requestAnimationFrame(tick); // 解析開始
    } catch (err) {
        alert("カメラの起動に失敗しました。設定を確認してください。");
    }
}

function tick() {
    const video = document.getElementById("video");
    const canvasElement = document.getElementById("canvas");
    const canvas = canvasElement.getContext("2d");

    if (video.readyState === video.HAVE_ENOUGH_DATA) {
        canvasElement.height = video.videoHeight;
        canvasElement.width = video.videoWidth;
        canvas.drawImage(video, 0, 0, canvasElement.width, canvasElement.height);
        
        const imageData = canvas.getImageData(0, 0, canvasElement.width, canvasElement.height);
        // 2. QRコードを解析
        const code = jsQR(imageData.data, imageData.width, imageData.height, {
            inversionAttempts: "dontInvert",
        });

        if (code) {
            // 3. 見つかったら合言葉をセットして終了
            document.getElementById("sync-keyword").value = code.data;
            alert("同期キーを読み込みました！：「" + code.data + "」");
            stopCamera();
            startSync(); // そのまま同期実行
            return;
        }
    }
    if (videoStream) requestAnimationFrame(tick);
}

function stopCamera() {
    if (videoStream) {
        videoStream.getTracks().forEach(track => track.stop());
        videoStream = null;
    }
    document.getElementById("camera-section").style.display = "none";
}
function findShelter() {
    // 1. 位置情報が使えるかチェック
    if (!navigator.geolocation) {
        alert("お使いのブラウザは位置情報に対応していません。");
        return;
    }
    console.log("位置情報を取得中...");

    // 2. 現在地を取得
    navigator.geolocation.getCurrentPosition(
        (position) => {
            const lat = position.coords.latitude;  // 緯度
            const lng = position.coords.longitude; // 経度
            const url = `https://www.google.co.jp/maps/search/避難所/@${lat},${lng},15z`;
            window.open(url, '_blank');
        },
        (error) => {
            // エラーハンドリング
            switch(error.code) {
                case error.PERMISSION_DENIED:
                    alert("位置情報の利用が許可されませんでした。設定から許可してください。");
                    break;
                case error.POSITION_UNAVAILABLE:
                    alert("現在地を取得できませんでした。");
                    break;
                case error.TIMEOUT:
                    alert("タイムアウトしました。再度お試しください。");
                    break;
                default:
                    alert("エラーが発生しました。");
                    break;
            }
        },
        {
            enableHighAccuracy: true, 
            timeout: 5000,           
            maximumAge: 0            
        }
    );
}