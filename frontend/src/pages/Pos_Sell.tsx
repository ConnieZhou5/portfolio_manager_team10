import React from 'react';
import { PortfolioStatsCards } from '../components/Stats';
import { Sells } from '../components/Sell';
import { Link } from 'react-router-dom';



function Sell() {

    return (

        <div className="bg-gradient-to-br from-purple-50 via-indigo-50 to-pink-50 min-h-screen">

            <div className="pt-6 bg-white sticky top-0 z-10 shadow-sm">

                <h1 className="text-4xl text-gray-600 mb-5 -ml-[950px]">My Portfolio</h1>

                <div className="space-x-8 text-lg font-medium text-gray-600 -ml-[940px]">
                    <Link to="/.">
                        <span className="hover:text-black cursor-pointer">Dashboard</span>
                    </Link>
                    <Link to="/Positions">
                        <span className="text-purple-500 border-b-2 border-purple-500 pb-1">Positions</span>
                    </Link>
                </div>
            </div>

            <div className="pl-10 pr-8 pt-3">
                <PortfolioStatsCards />
            </div>

            <div className="pl-10 pr-8 pb-10">
                <Sells />
            </div>

        </div>
    );

}


export { Sell }