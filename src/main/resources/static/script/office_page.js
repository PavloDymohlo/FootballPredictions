function formatDate(date) {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    return `${day}/${month}`;
}

function formatDateToUkrainian(dateStr) {
    const [day, month] = dateStr.split('/');
    const monthNames = [
        "січня", "лютого", "березня", "квітня", "травня", "червня",
        "липня", "серпня", "вересня", "жовтня", "листопада", "грудня"
    ];
    return `${day} ${monthNames[parseInt(month, 10) - 1]}`;
}

async function getUserName() {
    const spinner = document.getElementById('spinner');
    const userNameDiv = document.getElementById('user-name');
    spinner.style.display = 'block';
    const userName = await new Promise((resolve) => {
        setTimeout(() => {
            const storedUserName = localStorage.getItem('userName');
            resolve(storedUserName);
        }, 1000);
    });
    if (!userName) {
        console.error('User name not found in localStorage.');
        userNameDiv.textContent = 'Привіт, Користувач!';
    } else {
        userNameDiv.textContent = `Привіт, ${userName}!`;
    }
    spinner.style.display = 'none';
}

async function getMatchResult() {
    const spinner = document.getElementById('spinner');
    const resultsContainer = document.getElementById('resultsContainer');
    spinner.style.display = 'block';
    const userName = localStorage.getItem('userName');
    if (!userName) {
        console.error('User name not found in localStorage.');
        spinner.style.display = 'none';
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
        if (matchesResponse.status === 404) {
            const formattedDateUkrainian = formatDateToUkrainian(formattedDate);
            const message = `${formattedDateUkrainian} матчі не відбувалися.<br>Очікуйте наступних результатів.`;
            const container = document.createElement('div');
            container.className = 'date-group';
            container.innerHTML = message;
            container.style.textAlign = 'center';
            resultsContainer.appendChild(container);
            return;
        }
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
    } finally {
        spinner.style.display = 'none';
    }
}

async function displayMatchResult(matches, predictions) {
    const resultsContainer = document.getElementById('resultsContainer');
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const formattedDate = formatDate(yesterday);
    resultsContainer.innerHTML = '';

    const container = document.createElement('div');
    container.className = 'date-group';

    const header = document.createElement('h2');
    header.innerHTML = `
         <span style="line-height: 1.2;">Результати за ${formatDateToUkrainian(formattedDate)}</span><br>
        <div style="display: flex; flex-direction: column; align-items: center; max-width: 100%;">
          <p style="font-size: 14px; margin: 5px 0; text-align: center;">На зеленому фоні будуть відображені матчі результат яких ти відгадав.</p>
          <p style="font-size: 14px; margin: 5px 0; text-align: center;">Праворуч від результату в круглих дужках відображено твій прогноз.</p>
        </div>
    `;
    container.appendChild(header);

    if (!matches || matches.length === 0) {
        const noMatches = document.createElement('p');
        noMatches.textContent = 'Немає матчів з прогнозами.';
        container.appendChild(noMatches);
        resultsContainer.appendChild(container);
        return;
    }

    let currentCompetition = '';
    let competitionDiv;
    const predictionMap = new Map();
    const matchesContainer = document.createElement('div');
    matchesContainer.className = 'matches-container';

    if (predictions !== 'no_content') {
        predictions.forEach(item => {
            if (Array.isArray(item)) {
                const key = `${item[0].split(' ').slice(0, -1).join(' ')}_${item[1].split(' ').slice(0, -1).join(' ')}`;
                predictionMap.set(key, item);
            }
        });
    }

    matches[0].forEach(item => {
        if (item.country && item.tournament) {
            currentCompetition = `${item.country} - ${item.tournament}`;
            competitionDiv = document.createElement('div');
            const competitionHeader = document.createElement('h3');
            competitionHeader.textContent = currentCompetition;
            competitionDiv.appendChild(competitionHeader);
            matchesContainer.appendChild(competitionDiv);
        } else if (item.match && Array.isArray(item.match) && currentCompetition) {
            const matchDiv = document.createElement('div');
            matchDiv.className = 'match';

            if (item.predictedCorrectly) {
                matchDiv.classList.add('correct-prediction');
            }

            const team1Info = item.match[0].split(' ');
            const team2Info = item.match[1].split(' ');
            const team1Name = team1Info.slice(0, -1).join(' ');
            const team1Score = team1Info[team1Info.length - 1] === "?" ? "матч не відбувся" : team1Info[team1Info.length - 1];
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
            if (team1Score === "матч не відбувся") {
                score1.classList.add('canceled-match');
            }
            score1.textContent = `${team1Score} ${team1Score === "матч не відбувся" ? '' : (predictions === 'no_content' ? '(-)' : prediction ? `(${prediction[0].split(' ').pop()})` : '')}`;


            team1Div.appendChild(team1);
            team1Div.appendChild(score1);

            const team2Div = document.createElement('div');
            team2Div.className = 'team-score';

            const team2 = document.createElement('span');
            team2.className = 'team';
            team2.textContent = team2Name;

            const score2 = document.createElement('span');
            score2.className = 'score';
            score2.textContent = team2Score === "?" ? "" : `${team2Score} ${predictions === 'no_content' ? '(-)' : prediction ? `(${prediction[1].split(' ').pop()})` : ''}`;

            team2Div.appendChild(team2);
            team2Div.appendChild(score2);

            matchDiv.appendChild(team1Div);
            matchDiv.appendChild(team2Div);
            competitionDiv.appendChild(matchDiv);
        }
    });

    container.appendChild(matchesContainer);
    resultsContainer.appendChild(container);
}

async function getFutureMatches() {
    const spinner = document.getElementById('spinner');
    spinner.style.display = 'block';
    const userName = localStorage.getItem('userName');
    if (!userName) {
        console.error('User name not found in localStorage.');
        spinner.style.display = 'none';
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
    console.log(results);
    displayFutureMatches(results, dates);
    spinner.style.display = 'none';
}


function displayFutureMatches(results, dates) {
    const resultsContainer = document.getElementById('match-container');
    resultsContainer.innerHTML = '';
    const currentDate = new Date();
    const formattedCurrentDate = formatDate(currentDate);
    results.forEach((result, index) => {
        const dateGroup = document.createElement('div');
        dateGroup.className = 'date-group';
        const dateHeader = document.createElement('h2');
        const matchDate = dates[index];
        dateHeader.textContent = `Матчі на ${formatDateToUkrainian(dates[index])}`;
        dateGroup.appendChild(dateHeader);
        const isCurrentDate = matchDate === formattedCurrentDate;
        const predictions = [{ date: formatDateToUkrainian(dates[index]) }];
        if (!result || result.length === 0 || (result.length === 1 && result[0].length === 0)) {
            const message = document.createElement('div');
            message.className = 'message no-matches';
            message.innerHTML = `${formatDateToUkrainian(dates[index])} матчів не буде.<br>Очікуйте наступних подій.`;
            message.style.textAlign = 'center';
            dateGroup.appendChild(message);
        } else {
            let competitionContainer = document.createElement('div');
            competitionContainer.className = 'competition-container';
            let currentCompetition = null;
            let competitionDiv;
            result[0].forEach(item => {
                if (typeof item === 'object' && item.tournament && item.country) {
                    currentCompetition = { tournament: item.tournament, country: item.country };
                    predictions.push(currentCompetition);
                    competitionDiv = document.createElement('div');
                    const competitionHeader = document.createElement('h3');
                    competitionHeader.textContent = `${item.country}: ${item.tournament}`;
                    competitionDiv.appendChild(competitionHeader);
                    competitionContainer.appendChild(competitionDiv);
                }
                else if (Array.isArray(item) && currentCompetition) {
                    const matchDiv = document.createElement('div');
                    matchDiv.className = 'match';
                    const team1Div = document.createElement('div');
                    team1Div.className = 'team-score';
                    const team1 = document.createElement('span');
                    team1.className = 'team';
                    team1.textContent = item[0].replace(' ?', '');
                    const score1 = document.createElement('input');
                    score1.className = 'score';
                    score1.type = 'number';
                    score1.placeholder = '';
                    score1.style.width = '20px';
                    score1.style.height = '20px';
                    score1.style.textAlign = 'center';
                    score1.disabled = isCurrentDate;
                    team1Div.appendChild(team1);
                    team1Div.appendChild(score1);
                    const team2Div = document.createElement('div');
                    team2Div.className = 'team-score';
                    const team2 = document.createElement('span');
                    team2.className = 'team';
                    team2.textContent = item[1].replace(' ?', '');
                    const score2 = document.createElement('input');
                    score2.className = 'score';
                    score2.type = 'number';
                    score2.placeholder = '';
                    score2.style.width = '20px';
                    score2.style.height = '20px';
                    score2.style.textAlign = 'center';
                    score2.disabled = isCurrentDate;
                    team2Div.appendChild(team2);
                    team2Div.appendChild(score2);
                    matchDiv.appendChild(team1Div);
                    matchDiv.appendChild(team2Div);
                    competitionDiv.appendChild(matchDiv);
                    predictions.push([`${item[0].replace(' ?', '')} ${score1}`, `${item[1].replace(' ?', '')} ${score2}`]);
                    const separator = document.createElement('div');
                    separator.style.borderTop = '1px solid rgba(0, 0, 0, 0.8)';
                    separator.style.margin = '10px 0';
                    competitionDiv.appendChild(separator);
                }
            });
            const errorMessage = document.createElement('div');
            errorMessage.className = 'message error-message';
            errorMessage.textContent = 'Будь ласка, заповніть усі поля!';
            errorMessage.style.display = 'none';
            competitionContainer.appendChild(errorMessage);
            const submitButton = document.createElement('button');
            submitButton.className = 'submit-button';
            submitButton.textContent = 'Відправити';
            submitButton.disabled = isCurrentDate;
            competitionContainer.appendChild(submitButton);
            submitButton.addEventListener('click', () => {
                const userName = localStorage.getItem('userName');
                let allFilled = true;
                const matches = competitionContainer.querySelectorAll('.match');
                const successMessage = competitionContainer.querySelector('.success-message');
                if (successMessage) {
                    successMessage.remove();
                }
                predictions.splice(1);
                matches.forEach((match) => {
                    const scoreInputs = match.querySelectorAll('.score');
                    const score1 = scoreInputs[0].value.trim();
                    const score2 = scoreInputs[1].value.trim();
                    if (!score1) {
                        scoreInputs[0].style.border = '2px solid red';
                        allFilled = false;
                    } else {
                        scoreInputs[0].style.border = '';
                    }
                    if (!score2) {
                        scoreInputs[1].style.border = '2px solid red';
                        allFilled = false;
                    } else {
                        scoreInputs[1].style.border = '';
                    }
                    if (score1 && score2) {
                        const team1 = match.querySelectorAll('.team')[0].textContent;
                        const team2 = match.querySelectorAll('.team')[1].textContent;
                        predictions.push([`${team1} ${score1}`, `${team2} ${score2}`]);
                    }
                });
                if (!allFilled) {
                    errorMessage.style.display = 'block';
                } else {
                    errorMessage.style.display = 'none';
                    const request = {
                        userName: userName,
                        predictions: predictions,
                        matchDate: dates[index]
                    };
                    console.log("Дані, що відправляються:", request);
                    fetch('/user/send-predictions', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(request)
                    })
                        .then(response => {
                        if (response.ok) {
                            return response.text();
                        } else {
                            throw new Error('Network response was not ok.');
                        }
                    })
                        .then(() => {
                        const successMessage = document.createElement('div');
                        successMessage.className = 'message success-message';
                        successMessage.textContent = 'Прогноз успішно збережено!';
                        competitionContainer.appendChild(successMessage);
                    })
                        .catch(error => {
                        console.error('There was a problem with the fetch operation:', error);
                    });
                }
            });
            dateGroup.appendChild(competitionContainer);
        }
        resultsContainer.appendChild(dateGroup);
    });
}

function toggleMenu() {
    const submenu = document.getElementById('submenu');
    const burgerButton = document.getElementById('burgerButton');
    submenu.classList.toggle('open');
    if (submenu.style.display === 'block') {
        submenu.style.display = 'none';
        burgerButton.style.display = 'block';
    } else {
        submenu.style.display = 'block';
        burgerButton.style.display = 'none';
    }
}

function showStatistics() {
    event.preventDefault();
    window.location.href = '/statistic-page';
}

function showRules() {
    event.preventDefault();
    window.location.href = '/rules';
}

document.addEventListener('DOMContentLoaded', () => {
    getUserName();
    getFutureMatches();
    getMatchResult();
});