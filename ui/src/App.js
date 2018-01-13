import React, {Component} from 'react'
import math from 'mathjs'

import Client from "./Client"

import './App.css';

function Car(props) {
  const c = props.car
  const style = {
    backgroundColor: c.color,
    top: c.position.y,
    left: c.position.x,
    transform: 'rotate(' + c.orientation + 'deg)'
  }
  return (
    <div className={'Car ' + c.name} style={style}><span>{c.name} {c.life}-></span></div>
  )
}

class App extends Component {
  constructor(props) {
    super(props)
    this.state = {
      title: '',
      ws: null,
      owner: 'Ted',
      cars: []
    }
  }

  async componentDidMount() {
    Client.getSummary(summary => {
      this.setState({
        title: summary.content
      })
    })

    Client.openSocket.bind(this)()
    window.addEventListener('keydown', this.handleKeyDown.bind(this), false);
  }

  getOwnerData() {
    return this.state.cars.find(c => c.name === this.state.owner)
  }

  calcXY(orientation) {
    const angle = math.unit(orientation, 'deg'); // returns Unit 60 deg
    const position = {}
    position.x = math.cos(angle) * 10
    position.y = math.sin(angle) * 10
    return position
  }

  handleKeyDown(event) {
    const ownerData = this.getOwnerData()
    const gotoXY = this.calcXY(ownerData.orientation)
    switch (event.key) {
      case 'ArrowUp':
        ownerData.position.x += gotoXY.x
        ownerData.position.y += gotoXY.y

        break
      case 'ArrowDown':
        ownerData.position.x -= gotoXY.x
        ownerData.position.y -= gotoXY.y
        break
      case 'ArrowLeft':
        ownerData.orientation -= 10
        break
      case 'ArrowRight':
        ownerData.orientation += 10
        break
      default:
        break;
    }
    if (this.state.ws.closed) {
      Client.openSocket()
    }
    this.state.ws.send(JSON.stringify({request: 'update', car: ownerData}))
  }

  render() {
    return (
      <div className="App" onKeyUp={this.handleKeyDown}>
        <h1>{this.state.title}</h1>
        <div>{this.state.key}</div>
        <div className="Demo-floor">
          {this.state.cars.map(c => {
            return <Car car={c} key={c.name}></Car>
          })}
        </div>
      </div>
    )
  }
}

export default App;
