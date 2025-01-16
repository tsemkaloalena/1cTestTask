let stopBtn = document.getElementById("stopBtn");
let startBtn = document.getElementById("startBtn");

startBtn.addEventListener("click", async () => {
    await runStartBtnScript();
});

stopBtn.addEventListener("click", async () => {
    await runStopBtnScript();
});

async function runStartBtnScript() {
    let [tab] = await chrome.tabs.query({active: true, currentWindow: true});
    if (localStorage.getItem(tab.id) === null) {
        localStorage.setItem(tab.id, "");
        let results = await chrome.scripting.executeScript({
            target: {tabId: tab.id},
            func: setDigits
        }).then(injectionResults => {
            localStorage.setItem(tab.id, injectionResults[0].result);
        });
    }
}

async function runStopBtnScript() {
    let [tab] = await chrome.tabs.query({active: true, currentWindow: true});
    if (localStorage.getItem(tab.id) !== null) {
        await chrome.scripting.executeScript({
            target: {tabId: tab.id},
            func: backLetters,
            args: [localStorage.getItem(tab.id)]
        }).then(injectionResults => {
            localStorage.removeItem(tab.id);
        });
    }
}

function setDigits() {
    const all = document.querySelectorAll("*");
    const copyDocument = document.body.innerHTML;
    all.forEach((block) => {
        if (block.innerHTML !== undefined && block.innerHTML.length > 0) {
            var text = block.innerHTML;
            while (text.includes("а")) {
                text = text.replace("а", Math.floor(Math.random() * 9).toString());
            }
            block.innerHTML = text;
        }
    });
    return copyDocument;
}

function backLetters(bodyHTML) {
    document.body.innerHTML = bodyHTML;
}

