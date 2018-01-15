import React, {Component} from 'react'
import math from 'mathjs'

import Client from "./Client"

import './App.css';

function AddCar(props) {

  return (
    <div>
    <div>Add your car!</div>
    <form>
      <input type="text" value={props.nameValue} placeholder="FrankyStanky!" onChange={props.updateName}/>
      <input type="color" value={props.colorValue} onChange={props.updateColor}/>
      <input type="submit" value="Add a car!" onClick={props.submit}/>
    </form>
    </div>
  )
}

function OwnerStats(props) {
  return (
    <div>
      <h4>{props.ownerStats.name}</h4>
      <div><button onClick={props.deleteCar}>Delete Car</button></div>
      <div>{JSON.stringify(props.ownerStats)}</div>
    </div>
  )
}

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
    this.updateAddForm = this.updateAddForm.bind(this)
    this.updateName = this.updateName.bind(this)
    this.updateColor = this.updateColor.bind(this)
    this.addCar = this.addCar.bind(this)
    this.handleKeyDown = this.handleKeyDown.bind(this)
    this.getOwnerData = this.getOwnerData.bind(this)
    this.deleteCar = this.deleteCar.bind(this)
    this.state = {
      title: '',
      ws: null,
      authenticated: false,
      owner: null,
      cars: [],
      addCarForm: {
        name: '',
        color: '#66FF66'
      }
    }
  }

  async componentDidMount() {
    Client.openSocket.bind(this)(() =>  {
      window.addEventListener('keydown', this.handleKeyDown.bind(this), false)
      Client.authenticate.bind(this)(({userInfo, cars}) => {
        if (userInfo !== null) {
          this.setState({owner: userInfo.name}, () => this.setState({cars: cars}, () => this.setState({authenticated: true})))
        }
      })
    })
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
    if (!this.state.authenticated) return;
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
    if (this.state.ws.readyState !== 1) {
      Client.openSocket.bind(this)(() => {
        this.state.ws.send(JSON.stringify({request: 'update', car: ownerData}))
      })
    } else {
      this.state.ws.send(JSON.stringify({request: 'update', car: ownerData}))
    }
  }

  addCar(event) {
    Client.addCar(this.state.addCarForm, (cars) => {
      if (cars === 'Name already exists') {
        alert('Name already exists in game!');
      } else {
        localStorage.setItem('CAR_INFO', JSON.stringify(this.state.addCarForm))
        this.setState({cars: cars}, () => this.setState({owner: this.state.addCarForm.name}, this.setState({authenticated: true})))
      }
    })
    event.preventDefault()
  }

  updateAddForm(event, field) {
    const form = this.state.addCarForm
    form[field] = event.target.value.trim()
    this.setState({addCarForm: form})
  }

  updateName(event) { this.updateAddForm(event, 'name') }

  updateColor(event) { this.updateAddForm(event, 'color') }

  deleteCar() {
    this.state.ws.send(JSON.stringify({request: 'delete', car: this.getOwnerData()}))
    this.setState({authenticated: false})
    this.setState({owner: null})
    this.setState({addCarForm: {
      name: '',
      color: '#66FF66'
    }})
    localStorage.removeItem('CAR_INFO')
  }

  render() {
    let form;
    if (this.state.authenticated) {
      form = <OwnerStats ownerStats={this.getOwnerData()} deleteCar={this.deleteCar} />
    } else {
      form = <AddCar
        nameValue={this.state.addCarForm.name}
        updateName={this.updateName}
        colorValue={this.state.addCarForm.color}
        updateColor={this.updateColor}
        submit={this.addCar}/>
    }

    return (
      <div className="App" onKeyUp={this.handleKeyDown}>
        <h1>Demolition Derby</h1>
        <div>{this.state.key}</div>
        <div className="Demo-floor">
          {this.state.cars.map(c => {
            return <Car car={c} key={c.name}></Car>
          })}
        </div>
        {form}
      </div>
    )
  }
}

export default App;
