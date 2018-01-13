/* eslint-disable no-undef */
function getSummary(cb) {
  return fetch(`/summary`, {
    accept: "application/json"
  })
    .then(checkStatus)
    .then(parseJSON)
    .then(cb);
}

function checkStatus(response) {
  if (response.status >= 200 && response.status < 300) {
    return response;
  }
  const error = new Error(`HTTP Error ${response.statusText}`);
  error.status = response.statusText;
  error.response = response;
  console.log(error); // eslint-disable-line no-console
  throw error;
}

function parseJSON(response) {
  return response.json();
}


function openSocket(cb) {
  const ws = new WebSocket('ws://localhost:9000/cars')
  ws.onopen = () => {
    ws.send(JSON.stringify({request: 'cars'}))
    console.log('WS OPEN')
    if (cb) cb()
  }
  ws.onmessage = (msg) => this.setState({cars: JSON.parse(msg.data).cars})
  ws.onerror = (err) => console.error(err)
  ws.onclose = () => console.log('WS CLOSED')
  this.setState({ws: ws})
}

const Client = { getSummary, openSocket };
export default Client;
