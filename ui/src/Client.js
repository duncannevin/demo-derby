/* eslint-disable no-undef */

let ws;
const socketLoc = 'ws://localhost:9000/cars'

function openSocket(cb) {
  ws = new WebSocket(socketLoc)
  onOpen(() => {
    console.log('WS OPEN')
    cb()
  })
  onError((err) => console.error('WS FAILED', err))
  onClose(() => console.log('WS CLOSED'))
}

function authenticate(newCar, cb) {
  onMessage(cb)
  if (newCar !== null) {
    ws.send(JSON.stringify({request: 'authenticate', newCar: newCar}))
  } else  ws.send(JSON.stringify({request: 'cars'}))
}

function removeCar(car, cb) {
  onMessage(cb)
  ws.send(JSON.stringify({request: 'delete', car: car}))
}

function moveCar({ownerData, key}, cb) {
  onMessage(cb)
  if (ws.readyState !== 1) {
    openSocket.bind(this)(() => {
      ws.send(JSON.stringify({request: 'update', key: key, car: ownerData}))
    })
  } else {
    ws.send(JSON.stringify({request: 'update', key: key, car: ownerData}))
  }
}

function onOpen(cb) {
  ws.onopen = cb
}

function onMessage(cb) {
  ws.onmessage = cb
}

function onError(cb) {
  ws.onerror = cb
}

function onClose(cb) {
  ws.onclose = cb
}

const Client = { openSocket, authenticate, removeCar, moveCar };
export default Client;
