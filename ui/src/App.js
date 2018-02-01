import React, {Component} from 'react'

import Client from './Client'
import './App.css'

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

function TestCenter(props) {
  const c = props.car
  const style = {
    top: c.position.y,
    left: c.position.x
  }
  return (
    <div className="testCenter" style={style}></div>
  )
}

function Car(props) {
  const c = props.car
  const style = {
    backgroundColor: c.color,
    top: c.position.y - 15,
    left: c.position.x - 30,
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
    this.updateCars = this.updateCars.bind(this)
    this.state = {
      title: '',
      authenticated: false,
      cars: [],
      addCarForm: {
        name: '',
        color: '#66FF66'
      }
    }
  }

  async componentDidMount() {
    Client.openSocket(() => {
      const carInfo = JSON.parse(localStorage.getItem("CAR_INFO"))
      Client.authenticate.bind(this)(carInfo, (msg) => {
        this.updateCars(msg, (cars) => {
          const authenticated = !!carInfo
          if (authenticated) {
            this.setState({authenticated: authenticated, addCarForm: carInfo, cars: cars}, this.startKeyDown)
          } else this.setState({authenticated: authenticated, cars: cars})
        })
      })
    })
  }

  stopKeyDown() {
    window.removeEventListener('keydown', this.handleKeyDown)
  }

  startKeyDown() {
    window.addEventListener('keydown', this.handleKeyDown)
  }

  getOwnerData() {
    return this.state.cars.find(c => c.name === this.state.addCarForm.name)
  }

  handleKeyDown(event) {
    if (!this.state.authenticated) return;
    const ownerData = this.getOwnerData()
    Client.moveCar({ownerData: ownerData, key: event.key}, (msg) => {
      this.updateCars(msg, (cars) => {
        const name = this.state.addCarForm.name
        if (!cars.find(c => c.name === name)) {
          this.resetUserState(() => this.setState({cars: cars}))
        } else this.setState({cars: cars})
      })
    })
  }

  updateCars(msg, cb) {
    const cars = JSON.parse(msg.data)
    if (cb) cb(cars)
    else this.setState({cars: cars})
  }

  addCar(event) {
    event.preventDefault()
    const addCarForm = this.state.addCarForm
    Client.authenticate.bind(this)(addCarForm, (msg) => {
      localStorage.setItem("CAR_INFO", JSON.stringify(addCarForm))
      this.updateCars(msg, (cars) => {
        this.setState({cars: cars, authenticated: true}, this.startKeyDown)
      })
    })
  }

  updateAddForm(event, field) {
    const form = this.state.addCarForm
    form[field] = event.target.value.trim()
    this.setState({addCarForm: form})
  }

  updateName(event) { this.updateAddForm(event, 'name') }

  updateColor(event) { this.updateAddForm(event, 'color') }

  resetUserState(cb) {
    const emptyForm = {
      name: '',
      color: '#66FF66'
    }
    this.setState({authenticated: false, owner: null, addCarForm: emptyForm}, () => {
      this.stopKeyDown()
      localStorage.removeItem('CAR_INFO')
      if (cb) cb()
    })
  }

  deleteCar() {
    Client.removeCar(this.getOwnerData(), (msg) => {
      this.resetUserState()
      this.updateCars(msg)
    })
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
            return <TestCenter car={c} key={'test:' + c.name} />
          })}
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
