import React, { useState } from 'react';
import logo from './logo.svg';
import './App.css';
import { Dashboard } from './pages/Dashboard'
import { Positions } from './pages/Positions'
import { Sell } from './pages/Pos_Sell'
import { BrowserRouter, Routes, Route } from 'react-router-dom';


function App() {

  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/Positions" element={<Positions />} />
          <Route path="/Positions/Sell" element={<Sell />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
