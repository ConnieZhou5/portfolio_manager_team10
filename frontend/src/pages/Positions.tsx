
import React from 'react';
import { PortfolioStatsCards } from '../components/Stats';
import { Buys } from '../components/Buy';
import { Link } from 'react-router-dom';


function Positions() {


    return (
        <div className="bg-gradient-to-br from-purple-50 via-indigo-50 to-pink-50 min-h-screen">
            <div className="pt-6 pb-4 sticky top-0 z-50 backdrop-blur-md bg-white/70 border-b border-white/20 shadow-lg">
                <h1 className="text-4xl text-gray-700 mb-5 -ml-[950px] font-light tracking-wide">My Portfolio</h1>
                <div className="space-x-8 text-lg font-medium text-gray-600 -ml-[940px]">
                    <Link to="/.">
                        <span className="hover:text-purple-600 cursor-pointer transition-all duration-300 hover:scale-105">Dashboard</span>
                    </Link>
                    <Link to="/Positions">
                        <span className="text-purple-600 border-b-2 border-purple-500 pb-1 transition-all duration-300 hover:text-purple-700">Positions</span>
                    </Link>
                </div>
            </div>
            <div className="pl-10 pr-8 pt-3">
                <PortfolioStatsCards />
            </div>

            <div className="pl-10 pr-8 pb-10">
                <Buys />
            </div>

        </div>
    );

}

export { Positions }