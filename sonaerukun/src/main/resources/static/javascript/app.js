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

        createRow(name, date);
        sortItemsByDate(); 
        nameInput.value = '';
        dateInput.value = '';
        saveAllData();
    }

    // --- ★色分けロジック付きのcreateRow ---
    function createRow(name, date) {
        const tbody = document.getElementById('expiry-list-body');
        const tr = document.createElement('tr');
        
        tr.innerHTML = `
            <td><input type="text" class="expiry-name" value="${name}" onchange="saveAllData()"></td>
            <td><input type="month" class="expiry-date" value="${date}" onchange="saveAllData(); updateRowColor(this.parentElement.parentElement); sortItemsByDate();"></td>
            <td style="text-align: center;">
                <button type="button" onclick="this.parentElement.parentElement.remove(); saveAllData();" 
                    style="background: #fee2e2; color: #ef4444; border: none; border-radius: 6px; padding: 5px 10px; cursor: pointer; font-size: 0.8rem;">
                    削除
                </button>
            </td>
        `;
        tbody.appendChild(tr);
        updateRowColor(tr); // 追加時に色を判定
    }

    // --- ★色を塗る関数本体 ---
    function updateRowColor(tr) {
    const dateInput = tr.querySelector('.expiry-date');
    let statusLabel = tr.querySelector('.status-label');

    if (!statusLabel) {
        statusLabel = document.createElement('span');
        statusLabel.className = 'status-label';
        tr.children[0].appendChild(statusLabel);
    }

    if (!dateInput.value) return;

    const expiryDate = new Date(dateInput.value + "-01");
    const today = new Date();
    const currentMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const diffMonths = (expiryDate.getFullYear() - currentMonth.getFullYear()) * 12 + (expiryDate.getMonth() - currentMonth.getMonth());

    tr.classList.remove('status-danger', 'status-warning', 'status-safe');

    if (diffMonths < 1) {
        tr.classList.add('status-danger');
        statusLabel.textContent = "（期限切れ）";
    } else if (diffMonths < 3) {
        tr.classList.add('status-warning');
        statusLabel.textContent = "（注意）";
    } else {
        tr.classList.add('status-safe');
        statusLabel.textContent = "（安全）";
    }
}

    // 保存・同期処理
    function saveAllData() {
        const names = document.querySelectorAll('.expiry-name');
        const dates = document.querySelectorAll('.expiry-date');
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
        
        database.ref('users/' + keyword).once('value').then((snapshot) => {
            if (snapshot.exists()) {
                reflectDataToUI(snapshot.val());
                alert("家族のデータと同期しました！");
            } else {
                saveAllData();
                alert("新しいグループを作成しました！");
            }
            observeData();
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
        const tbody = document.getElementById('expiry-list-body');
        if (data.items) {
            tbody.innerHTML = ''; 
            data.items.forEach(item => {
                createRow(item.name, item.date);
            });
        }
        if (data.memo) {
            document.getElementById('evac-place').value = data.memo.place || '';
            document.getElementById('family-rule').value = data.memo.rule || '';
            document.getElementById('emergency-tel').value = data.memo.tel || '';
        }
    }

    // ページ読み込み時の処理
    window.onload = function() {
        const local = JSON.parse(localStorage.getItem('sonaerukun_v3_data') || '{}');
        if (local.items || local.memo) {
            reflectDataToUI(local);
        }
        const savedKeyword = localStorage.getItem('sonaerukun_keyword');
        if (savedKeyword) {
            document.getElementById('sync-keyword').value = savedKeyword;
            currentKeyword = savedKeyword;
            observeData();
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
    const tbody = document.getElementById('expiry-list-body');
    const rows = Array.from(tbody.querySelectorAll('tr'));

    rows.sort((a, b) => {
        const dateA = a.querySelector('.expiry-date').value;
        const dateB = b.querySelector('.expiry-date').value;

        // 空は一番下に
        if (!dateA) return 1;
        if (!dateB) return -1;

        const diffA = new Date(dateA) - new Date();
const diffB = new Date(dateB) - new Date();

return diffA - diffB;
    });

    // 並び替えた順に再配置
    rows.forEach(row => tbody.appendChild(row));
}