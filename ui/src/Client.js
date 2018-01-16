/* eslint-disable no-undef */
function addCar(body, cb) {
  return fetch(`/addCar`, {
    method: `POST`,
    headers: {
      'Accept': 'application/json, text/plain, */*',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(body)
  })
    .then(checkStatus)
    .then(parseJSON)
    .then(cb)
}

function checkStatus(response) {
  if (response.status >= 200 && response.status < 300 && response.status !== 409) {
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

function authenticate(cb) {
  const session = JSON.parse(localStorage.getItem("CAR_INFO"))
  if (session === null) {
    cb({userInfo: null, cars: null})
  } else {
    addCar(session, (cars) => {
      if (cars === 'Name already exists') {
        cb({userInfo: session, cars: this.state.cars})
      } else {
        // console.log(cars)
        cb({userInfo: session, cars: cars})
      }
    })
  }
}

function openSocket(cb) {
  const ws = new WebSocket('ws://localhost:9000/cars')
  ws.onopen = () => {
    ws.send(JSON.stringify({request: 'cars'}))
    console.log('WS OPEN')
    if (cb) cb()
  }
  ws.onmessage = (msg) => this.setState({cars: JSON.parse(msg.data)})
  ws.onerror = (err) => console.error(err)
  ws.onclose = () => console.log('WS CLOSED')
  this.setState({ws: ws})
}

const Client = { openSocket, addCar, authenticate };
export default Client;
