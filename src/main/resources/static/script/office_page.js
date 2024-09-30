function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

async function getMatchResult() {
    const userName = localStorage.getItem('userName');
    if (!userName) {
        console.error('User name not found in localStorage.');
        return;
    }
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const formattedDate = formatDate(yesterday);
    try {
        const [matchesResponse, predictionsResponse] = await Promise.all([
            fetch(`/user/match-status?date=${formattedDate}`, {
                method: 'GET',
                headers: {
                    'userName': userName,
                },
            }),
            fetch(`/user/get-predictions?date=${formattedDate}`, {
                method: 'GET',
                headers: {
                    'userName': userName,
                },
            })
        ]);
        const matches = await matchesResponse.json();
        let predictions;
        if (predictionsResponse.status === 204) {
            predictions = { predictions: 'no_content' };
        } else {
            predictions = await predictionsResponse.json();
        }
        displayMatchResult(matches, predictions.predictions);
    } catch (error) {
        console.error('Error fetching match status or predictions:', error);
    }
}

async function displayMatchResult(matches, predictions) {
    const resultsContainer = document.getElementById('resultsContainer');
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const formattedDate = formatDate(yesterday);
    resultsContainer.innerHTML = '';

    // Create header
    const header = document.createElement('h2');
    header.className = 'competition';
    header.innerHTML = `
        <span style="line-height: 1.2;">Результати за ${formattedDate}</span><br>
        <span style="font-size: small; line-height: 1.2; margin-top: 4px;">На зеленому фоні будуть відображені матчі результат яких ти відгадав.</span><br>
        <span style="font-size: small; line-height: 1.2; margin-top: 4px;">Праворуч від результату в круглих дужках відображено твій прогноз.</span>
    `;

    resultsContainer.appendChild(header);



    if (!matches || matches.length === 0) {
        const noMatches = document.createElement('p');
        noMatches.textContent = 'Немає матчів з прогнозами.';
        resultsContainer.appendChild(noMatches);
        return;
    }
    let currentCompetition = null;
    let competitionDiv;
    const predictionMap = new Map();
    if (predictions !== 'no_content') {
        predictions.forEach(item => {
            if (Array.isArray(item)) {
                const key = `${item[0].split(' ').slice(0, -1).join(' ')}_${item[1].split(' ').slice(0, -1).join(' ')}`;
                predictionMap.set(key, item);
            }
        });
    }
    matches[0].forEach(item => {
        if (typeof item === 'object' && item.competition) {
            currentCompetition = item.competition;
            competitionDiv = document.createElement('div');
            competitionDiv.className = 'competition';
            const competitionHeader = document.createElement('h3');
            competitionHeader.textContent = currentCompetition;
            competitionDiv.appendChild(competitionHeader);
            resultsContainer.appendChild(competitionDiv);
        } else if (typeof item === 'object' && item.match && currentCompetition) {
            const matchDiv = document.createElement('div');
            matchDiv.className = 'match';
            if (item.predictedCorrectly) {
                matchDiv.classList.add('correct-prediction');
            }
            const team1Info = item.match[0].split(' ');
            const team2Info = item.match[1].split(' ');
            const team1Name = team1Info.slice(0, -1).join(' ');
            const team1Score = team1Info[team1Info.length - 1];
            const team2Name = team2Info.slice(0, -1).join(' ');
            const team2Score = team2Info[team2Info.length - 1];
            const predictionKey = `${team1Name}_${team2Name}`;
            const prediction = predictions === 'no_content' ? null : predictionMap.get(predictionKey);
            const team1Div = document.createElement('div');
            team1Div.className = 'team-score';
            const team1 = document.createElement('span');
            team1.className = 'team';
            team1.textContent = team1Name;
            const score1 = document.createElement('span');
            score1.className = 'score';
            score1.textContent = `${team1Score} ${predictions === 'no_content' ? '(-)' : prediction ? `(${prediction[0].split(' ').pop()})` : ''}`;
            team1Div.appendChild(team1);
            team1Div.appendChild(score1);
            const team2Div = document.createElement('div');
            team2Div.className = 'team-score';
            const team2 = document.createElement('span');
            team2.className = 'team';
            team2.textContent = team2Name;
            const score2 = document.createElement('span');
            score2.className = 'score';
            score2.textContent = `${team2Score} ${predictions === 'no_content' ? '(-)' : prediction ? `(${prediction[1].split(' ').pop()})` : ''}`;
            team2Div.appendChild(team2);
            team2Div.appendChild(score2);
            matchDiv.appendChild(team1Div);
            matchDiv.appendChild(team2Div);
            competitionDiv.appendChild(matchDiv);
        }
    });
}








async function getFutureMatches() {
    const userName = localStorage.getItem('userName');
    if (!userName) {
        console.error('User name not found in localStorage.');
        return;
    }
    const results = [];
    const dates = [];
    for (let i = 0; i <= 3; i++) {
        const date = new Date();
        date.setDate(date.getDate() + i);
        const formattedDate = formatDate(date);
        dates.push(formattedDate);
        try {
            const response = await fetch(`/user/event?date=${formattedDate}`, {
                method: 'GET',
                headers: {
                    'userName': userName,
                },
            });
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();
            results.push(data);
        } catch (error) {
            console.error('Error fetching data:', error);
        }
    }
    displayFutureMatches(results, dates);
}

function displayFutureMatches(results, dates) {
    const resultsContainer = document.getElementById('match-container');
    resultsContainer.innerHTML = '';

    results.forEach((result, index) => {
        const dateGroup = document.createElement('div');
        dateGroup.className = 'date-group';

        const dateHeader = document.createElement('h2');
        dateHeader.className = 'competition';
        dateHeader.textContent = `Матчі на ${dates[index]}`;
        dateGroup.appendChild(dateHeader);

        if (!result || result.length === 0 || (result.length === 1 && result[0].length === 0)) {
            const noMatches = document.createElement('p');
            noMatches.className = 'competition';
            noMatches.textContent = 'На цей день матчів немає.';
            dateGroup.appendChild(noMatches);
        } else {
            let currentCompetition = null;
            let competitionDiv;

            result[0].forEach(item => {
                if (typeof item === 'object' && item.competition) {
                    currentCompetition = item.competition;
                    competitionDiv = document.createElement('div');
                    competitionDiv.className = 'competition';
                    const competitionHeader = document.createElement('h3');
                    competitionHeader.textContent = currentCompetition;
                    competitionDiv.appendChild(competitionHeader);
                    dateGroup.appendChild(competitionDiv);
                } else if (Array.isArray(item) && currentCompetition) {
                    const matchDiv = document.createElement('div');
                    matchDiv.className = 'match';

                    const team1Div = document.createElement('div');
                    team1Div.className = 'team-score';
                    const team1 = document.createElement('span');
                    team1.className = 'team';
                    team1.textContent = item[0].replace(' ?', '');
                    const score1 = document.createElement('span');
                    score1.className = 'score';
                    score1.textContent = '?';
                    team1Div.appendChild(team1);
                    team1Div.appendChild(score1);

                    const team2Div = document.createElement('div');
                    team2Div.className = 'team-score';
                    const team2 = document.createElement('span');
                    team2.className = 'team';
                    team2.textContent = item[1].replace(' ?', '');
                    const score2 = document.createElement('span');
                    score2.className = 'score';
                    score2.textContent = '?';
                    team2Div.appendChild(team2);
                    team2Div.appendChild(score2);

                    matchDiv.appendChild(team1Div);
                    matchDiv.appendChild(team2Div);
                    competitionDiv.appendChild(matchDiv);
                }
            });
        }

        resultsContainer.appendChild(dateGroup);
    });
}




// просто джсон
//function displayResults(results, dates) {
//    const resultsContainer = document.getElementById('match-container');
//    resultsContainer.innerHTML = '';
//    results.forEach((result, index) => {
//        const resultDiv = document.createElement('div');
//        resultDiv.className = 'result';
//        if (!result || result.length === 0 || (result.length === 1 && result[0].length === 0)) {
//            resultDiv.innerHTML = `<h3>Матчі на ${dates[index]}:</h3><p>На цей день матчів немає.</p>`;
//        } else {
//            resultDiv.innerHTML = `<h3>Матчі на ${dates[index]}:</h3><pre>${JSON.stringify(result, null, 2)}</pre>`;
//        }
//        resultsContainer.appendChild(resultDiv);
//    });
//}





document.addEventListener('DOMContentLoaded', () => {
   getFutureMatches();
    getMatchResult();
});