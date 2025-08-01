import React, {useState} from 'react';
import logo from './logo.svg';
import './App.css';
import { Dashboard } from './pages/Dashboard'
import { Positions } from './pages/Positions'

function App() {

  return (
    <div className="App">
      <Dashboard />
      {/* <Positions /> */}
    </div>
  );
}

export default App;
