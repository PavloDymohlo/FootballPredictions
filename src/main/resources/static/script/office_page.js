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
    console.log("Matches data:", matches);
    console.log("Predictions data:", predictions);

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

    // Process predictions and create a lookup map
    // The predictions may be nested in an array if they come from Collections.singletonList
    const predictionData = Array.isArray(predictions) && predictions.length > 0 && Array.isArray(predictions[0])
    ? predictions[0]
    : predictions;

    if (predictionData && predictionData !== 'no_content') {
        // Debug logs
        console.log("Processing predictions data:", predictionData);

        // Iterate through each prediction item
        predictionData.forEach(item => {
            // Check if it's a match prediction (array of two strings)
            if (Array.isArray(item) && item.length === 2 && typeof item[0] === 'string' && typeof item[1] === 'string') {
                // Extract exact team names as they appear in the prediction
                const homeTeam = item[0].split(' ').slice(0, -1).join(' ');
                const awayTeam = item[1].split(' ').slice(0, -1).join(' ');
                // Create various possible key formats
                const key1 = `${homeTeam}_${awayTeam}`;
                const key2 = `${homeTeam.trim()}_${awayTeam.trim()}`;

                // Store prediction with multiple keys to increase chances of matching
                console.log(`Creating prediction map entry: ${key1} -> ${item[0]} vs ${item[1]}`);
                predictionMap.set(key1, item);
                predictionMap.set(key2, item);

                // Also store by individual team names
                predictionMap.set(homeTeam, item);
                predictionMap.set(awayTeam, item);
            }
        });
    }

    console.log("Final prediction map size:", predictionMap.size);

    // Process matches
    // The matches may be nested in an array if they come from Collections.singletonList
    const matchData = Array.isArray(matches) && matches.length > 0 && Array.isArray(matches[0])
    ? matches[0]
    : matches;

    if (Array.isArray(matchData)) {
        // Debug logs
        console.log("Processing match data array with length:", matchData.length);

        matchData.forEach(item => {
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

                // Parse team and score information
                // Оновлена функція parseTeamAndScore
                const parseTeamAndScore = (matchText) => {
                    if (!matchText) return { teamName: '', score: '?', fullScore: '?' };

                    // Перевіряємо, чи містить текст слово "Переможець"
                    if (matchText.includes('Переможець')) {
                        // Видаляємо слово "Переможець"
                        let processedText = matchText.replace('Переможець', '').trim();

                        // Шукаємо числовий рахунок у тексті (зазвичай останнє число в рядку)
                        const scoreMatch = processedText.match(/(\d+)(?:\s*)$/);
                        let teamName = processedText;
                        let score = '?';

                        if (scoreMatch) {
                            // Якщо знайдено рахунок, видаляємо його з назви команди
                            score = scoreMatch[1];
                            teamName = processedText.replace(scoreMatch[0], '').trim();
                        }

                        // Змінюємо назву команди, додаючи до неї " (Прохід далі)"
                        teamName = `${teamName}  (Прохід далі)`;

                        return {
                            teamName: teamName,
                            score: score,
                            fullScore: score
                        };
                    }

                    // Check if there's additional info in brackets (extra time, penalties)
                    const hasBrackets = matchText.includes('(');

                    if (!hasBrackets) {
                        const parts = matchText.trim().split(' ');
                        const score = parts.pop() || '?';
                        const teamName = parts.join(' ');
                        return {
                            teamName: teamName,
                            score: score,
                            fullScore: score
                        };
                    }

                    // Extract main content and bracketed content
                    const bracketIndex = matchText.indexOf('(');
                    const beforeBrackets = matchText.substring(0, bracketIndex).trim();
                    const parts = beforeBrackets.split(' ');
                    const mainScore = parts.pop() || '?';
                    const teamName = parts.join(' ');
                    const extraInfo = matchText.substring(bracketIndex);

                    return {
                        teamName: teamName,
                        score: mainScore.trim(),
                        fullScore: `${mainScore} ${extraInfo}`
                    };
                };

                const team1Info = parseTeamAndScore(item.match[0]);
                const team2Info = parseTeamAndScore(item.match[1]);

                console.log(`Match teams: ${team1Info.teamName} vs ${team2Info.teamName}`);

                // Create lookup keys for predictions
                const predictionKey = `${team1Info.teamName}_${team2Info.teamName}`;
                const fallbackKey = team1Info.teamName; // Use home team as fallback

                console.log(`Looking up prediction for key: ${predictionKey}`);

                // Get user prediction for this match, with fallbacks
                let prediction = predictionMap.get(predictionKey);
                if (!prediction) {
                    prediction = predictionMap.get(fallbackKey);
                    console.log(`Using fallback key: ${fallbackKey}`);
                }

                // If we still don't have a prediction, try to find it in the raw data
                if (!prediction && Array.isArray(predictionData) && predictionData !== 'no_content') {
                    // Try to find by partial match on team names
                    prediction = predictionData.find(pred => {
                        if (Array.isArray(pred) && pred.length === 2) {
                            const homeTeamText = pred[0].toLowerCase();
                            const awayTeamText = pred[1].toLowerCase();
                            return homeTeamText.includes(team1Info.teamName.toLowerCase()) ||
                            awayTeamText.includes(team2Info.teamName.toLowerCase());
                        }
                        return false;
                    });
                }

                console.log(`Found prediction:`, prediction);

                // Use prediction from item if available
                if (item.userPrediction && Array.isArray(item.userPrediction) && item.userPrediction.length === 2) {
                    prediction = item.userPrediction;
                    console.log("Using prediction from match item:", prediction);
                }

                // Home team display
                const team1Div = document.createElement('div');
                team1Div.className = 'team-score';

                const team1 = document.createElement('span');
                team1.className = 'team';
                team1.textContent = team1Info.teamName;

                const score1 = document.createElement('span');
                score1.className = 'score';

                if (team1Info.score === "матч не відбувся") {
                    score1.classList.add('canceled-match');
                    score1.textContent = team1Info.score;
                } else {
                    // Prepare the prediction text
                    let predictionText = '(-)';

                    if (prediction) {
                        // Try different methods to extract the score
                        let predScore = '';

                        if (Array.isArray(prediction) && prediction.length === 2) {
                            // Try to extract score from the end of the string
                            const parts = prediction[0].split(' ');
                            predScore = parts[parts.length - 1];

                            // If predScore is not a number, try to find a number in the string
                            if (isNaN(predScore)) {
                                const match = prediction[0].match(/\d+/);
                                if (match) {
                                    predScore = match[0];
                                }
                            }
                        }

                        // If we found a valid prediction score
                        if (predScore && !isNaN(predScore)) {
                            predictionText = `(${predScore})`;
                            console.log(`Using prediction score for team 1: ${predScore}`);
                        }
                    }

                    score1.textContent = `${team1Info.fullScore} ${predictionText}`;
                }

                team1Div.appendChild(team1);
                team1Div.appendChild(score1);

                // Away team display
                const team2Div = document.createElement('div');
                team2Div.className = 'team-score';

                const team2 = document.createElement('span');
                team2.className = 'team';
                team2.textContent = team2Info.teamName;

                const score2 = document.createElement('span');
                score2.className = 'score';

                if (team2Info.score === "?") {
                    score2.textContent = "";
                } else {
                    // Prepare the prediction text
                    let predictionText = '(-)';

                    if (prediction) {
                        // Try different methods to extract the score
                        let predScore = '';

                        if (Array.isArray(prediction) && prediction.length === 2) {
                            // Try to extract score from the end of the string
                            const parts = prediction[1].split(' ');
                            predScore = parts[parts.length - 1];

                            // If predScore is not a number, try to find a number in the string
                            if (isNaN(predScore)) {
                                const match = prediction[1].match(/\d+/);
                                if (match) {
                                    predScore = match[0];
                                }
                            }
                        }

                        // If we found a valid prediction score
                        if (predScore && !isNaN(predScore)) {
                            predictionText = `(${predScore})`;
                            console.log(`Using prediction score for team 2: ${predScore}`);
                        }
                    }

                    score2.textContent = `${team2Info.fullScore} ${predictionText}`;
                }

                team2Div.appendChild(team2);
                team2Div.appendChild(score2);

                matchDiv.appendChild(team1Div);
                matchDiv.appendChild(team2Div);
                competitionDiv.appendChild(matchDiv);
            }
        });
    }

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
    const userName = localStorage.getItem('userName');

    results.forEach((result, index) => {
        const dateGroup = document.createElement('div');
        dateGroup.className = 'date-group';

        // Створюємо заголовок з можливістю клікати
        const dateHeader = document.createElement('h2');
        const matchDate = dates[index];
        dateHeader.innerHTML = `
            <span style="display: flex; justify-content: space-between; align-items: center;">
                Матчі на ${formatDateToUkrainian(dates[index])}
                <span class="toggle-icon">▼</span>
            </span>
        `;
        dateHeader.style.cursor = 'pointer';
        dateGroup.appendChild(dateHeader);

        // Створюємо контейнер для вмісту
        const contentContainer = document.createElement('div');
        contentContainer.className = 'collapsible-content';
        contentContainer.style.display = 'none';

        // Додаємо подію кліку для розгортання/згортання
        dateHeader.addEventListener('click', function() {
            if (contentContainer.style.display === 'none') {
                contentContainer.style.display = 'block';
                dateHeader.querySelector('.toggle-icon').textContent = '▲';
            } else {
                contentContainer.style.display = 'none';
                dateHeader.querySelector('.toggle-icon').textContent = '▼';
            }
        });

        const isCurrentDate = matchDate === formattedCurrentDate;
        const predictions = [{ date: formatDateToUkrainian(dates[index]) }];

        // Перевіряємо, чи є збережені прогнози в localStorage
        const storageKey = `userPredictions_${matchDate}_${userName}`;
        const savedPredictions = localStorage.getItem(storageKey);

        if (savedPredictions) {
            // Якщо є збережені прогнози, відображаємо їх замість стандартного вмісту
            contentContainer.innerHTML = savedPredictions;
        } else {
            // Якщо немає збережених прогнозів, відображаємо стандартний вміст
            if (!result || result.length === 0 || (result.length === 1 && result[0].length === 0)) {
                const message = document.createElement('div');
                message.className = 'message no-matches';
                message.innerHTML = `${formatDateToUkrainian(dates[index])} матчів не буде.<br>Очікуйте наступних подій.`;
                message.style.textAlign = 'center';
                contentContainer.appendChild(message);
            } else {
                let competitionContainer = document.createElement('div');
                competitionContainer.className = 'competition-container';
                let currentCompetition = null;
                let competitionDiv;

                // Створюємо масив для зберігання посилань на матчі та поля вводу
                const matchesData = [];

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

                        // Створюємо об'єкт для зберігання даних про матч
                        const matchData = {
                            div: matchDiv,
                            teams: {},
                            inputs: {}
                        };

                        // Обробка для команди 1
                        const team1Div = document.createElement('div');
                        team1Div.className = 'team-score';
                        const team1 = document.createElement('span');
                        team1.className = 'team';
                        const team1Name = item[0].replace(' ?', '').replace(/ 0$/, '');
                        team1.textContent = team1Name;
                        matchData.teams.team1 = team1Name;

                        const score1 = document.createElement('input');
                        score1.className = 'score';
                        score1.type = 'number';
                        score1.placeholder = '';
                        score1.style.width = '20px';
                        score1.style.height = '20px';
                        score1.style.textAlign = 'center';
                        score1.disabled = isCurrentDate;
                        matchData.inputs.score1 = score1;

                        team1Div.appendChild(team1);
                        team1Div.appendChild(score1);

                        // Обробка для команди 2
                        const team2Div = document.createElement('div');
                        team2Div.className = 'team-score';
                        const team2 = document.createElement('span');
                        team2.className = 'team';
                        const team2Name = item[1].replace(' ?', '').replace(/ 0$/, '');
                        team2.textContent = team2Name;
                        matchData.teams.team2 = team2Name;

                        const score2 = document.createElement('input');
                        score2.className = 'score';
                        score2.type = 'number';
                        score2.placeholder = '';
                        score2.style.width = '20px';
                        score2.style.height = '20px';
                        score2.style.textAlign = 'center';
                        score2.disabled = isCurrentDate;
                        matchData.inputs.score2 = score2;

                        team2Div.appendChild(team2);
                        team2Div.appendChild(score2);

                        matchDiv.appendChild(team1Div);
                        matchDiv.appendChild(team2Div);
                        competitionDiv.appendChild(matchDiv);

                        // Додаємо дані про матч до масиву
                        matchesData.push(matchData);

                        predictions.push([`${team1Name} ${score1.value}`, `${team2Name} ${score2.value}`]);

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
                    const successMessage = competitionContainer.querySelector('.success-message');
                    if (successMessage) {
                        successMessage.remove();
                    }

                    predictions.splice(1);
                    const actualPredictions = [];

                    // Перевіряємо всі матчі
                    matchesData.forEach((matchData) => {
                        const score1 = matchData.inputs.score1.value.trim();
                        const score2 = matchData.inputs.score2.value.trim();

                        // Зберігаємо значення прогнозів для цього матчу
                        const matchPrediction = {
                            team1: matchData.teams.team1,
                            team2: matchData.teams.team2,
                            score1: score1,
                            score2: score2
                        };
                        actualPredictions.push(matchPrediction);

                        if (!score1) {
                            matchData.inputs.score1.style.border = '2px solid red';
                            allFilled = false;
                        } else {
                            matchData.inputs.score1.style.border = '';
                        }

                        if (!score2) {
                            matchData.inputs.score2.style.border = '2px solid red';
                            allFilled = false;
                        } else {
                            matchData.inputs.score2.style.border = '';
                        }

                        if (score1 && score2) {
                            predictions.push([`${matchData.teams.team1} ${score1}`, `${matchData.teams.team2} ${score2}`]);
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
                            // Створюємо повідомлення про успіх
                            const successMessage = document.createElement('div');
                            successMessage.className = 'message success-message';
                            successMessage.textContent = 'Прогноз успішно збережено!';
                            competitionContainer.appendChild(successMessage);

                            // Замінюємо поля вводу на статичний текст з прогнозами
                            matchesData.forEach((matchData, idx) => {
                                const prediction = actualPredictions[idx];
                                if (prediction && prediction.score1 && prediction.score2) {
                                    // Замінюємо поле вводу на текст з прогнозом для першої команди
                                    const team1Div = matchData.div.querySelector('.team-score:first-child');
                                    const score1Input = team1Div.querySelector('.score');
                                    team1Div.removeChild(score1Input);

                                    const score1Text = document.createElement('span');
                                    score1Text.className = 'score-prediction';
                                    score1Text.textContent = prediction.score1;
                                    score1Text.style.marginLeft = '5px';
                                    score1Text.style.fontWeight = 'bold';
                                    team1Div.appendChild(score1Text);

                                    // Замінюємо поле вводу на текст з прогнозом для другої команди
                                    const team2Div = matchData.div.querySelector('.team-score:last-child');
                                    const score2Input = team2Div.querySelector('.score');
                                    team2Div.removeChild(score2Input);

                                    const score2Text = document.createElement('span');
                                    score2Text.className = 'score-prediction';
                                    score2Text.textContent = prediction.score2;
                                    score2Text.style.marginLeft = '5px';
                                    score2Text.style.fontWeight = 'bold';
                                    team2Div.appendChild(score2Text);
                                }
                            });

                            // Вимикаємо кнопку відправки після збереження
                            submitButton.disabled = true;
                            submitButton.textContent = 'Прогноз збережено';

                            // Зберігаємо HTML-вміст контейнера після всіх змін
                            localStorage.setItem(storageKey, contentContainer.innerHTML);
                        })
                            .catch(error => {
                            console.error('There was a problem with the fetch operation:', error);
                        });
                    }
                });

                contentContainer.appendChild(competitionContainer);
            }
        }

        // Додаємо контейнер з контентом до групи дати
        dateGroup.appendChild(contentContainer);
        resultsContainer.appendChild(dateGroup);
    });

    // Додаємо стилі
    const style = document.createElement('style');
    style.textContent = `
        .date-group h2 {
            background-color: #f5f5f5;
            padding: 10px;
            margin: 5px 0;
            border-radius: 5px;
            transition: background-color 0.3s;
        }

        .date-group h2:hover {
            background-color: #e5e5e5;
        }

        .toggle-icon {
            font-size: 12px;
            transition: transform 0.3s;
        }

        .collapsible-content {
            padding: 10px;
            border: 1px solid #e5e5e5;
            border-top: none;
            border-radius: 0 0 5px 5px;
            margin-bottom: 10px;
        }

        .score-prediction {
            display: inline-block;
            width: 20px;
            height: 20px;
            text-align: center;
            color: #3366cc;
        }

        .success-message {
            background-color: #d4edda;
            color: #155724;
            padding: 10px;
            margin-top: 10px;
            border-radius: 4px;
        }
    `;
    document.head.appendChild(style);
}

function toggleMenu() {
    const submenu = document.getElementById('submenu');
    const burgerButton = document.getElementById('burgerButton');
    const overlay = document.getElementById('overlay');

    // Додати/видалити класи для анімації
    submenu.classList.toggle('open');

    // Показати/сховати меню з плавнішою анімацією
    if (submenu.style.display === 'block') {
        // Приховуємо меню
        overlay.classList.remove('active');

        // Спочатку зникає overlay, потім меню
        setTimeout(() => {
            submenu.style.display = 'none';
            overlay.style.display = 'none';

            // Важливо: повертаємо видимість бургеру після закриття меню
            setTimeout(() => {
                burgerButton.style.opacity = '1';
                burgerButton.style.visibility = 'visible';
            }, 100);
        }, 500);
    } else {
        // Ховаємо бургер при відкритті меню
        burgerButton.style.opacity = '0';
        burgerButton.style.visibility = 'hidden';

        submenu.style.display = 'block';
        overlay.style.display = 'block';

        // Затримка для початку анімації
        setTimeout(() => {
            overlay.classList.add('active');
        }, 10);
    }
}


function showStatistics() {
    // Закриваємо меню після вибору опції
    toggleMenu();
    window.location.href = '/statistic-page';
}


function showRules() {
    // Закриваємо меню після вибору опції
    toggleMenu();
    window.location.href = '/rules';
}

function closeMenuOutsideClick() {
    const overlay = document.getElementById('overlay');
    overlay.addEventListener('click', toggleMenu);
}

// Функція для обробки клікa на хрестик
function closeMenu() {
    const submenu = document.getElementById('submenu');
    const burgerButton = document.getElementById('burgerButton');
    const overlay = document.getElementById('overlay');

    // Видаляємо клас open для анімації закриття
    submenu.classList.remove('open');
    overlay.classList.remove('active');

    // Анімуємо закриття меню
    setTimeout(() => {
        submenu.style.display = 'none';
        overlay.style.display = 'none';

        // Повертаємо видимість бургеру
        setTimeout(() => {
            burgerButton.style.opacity = '1';
            burgerButton.style.visibility = 'visible';
        }, 100);
    }, 500);
}

document.addEventListener('DOMContentLoaded', () => {
    getUserName();
    getFutureMatches();
    getMatchResult();
    closeMenuOutsideClick(); // Додаємо обробник для закриття меню при кліку поза ним
});