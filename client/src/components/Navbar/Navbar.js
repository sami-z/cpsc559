import React from 'react'
import './styles.css'
import DFSLogo from '../../media/FileArc Logo.png'
import { useState } from 'react'

import SearchIcon from '@mui/icons-material/Search';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import SettingsIcon from '@mui/icons-material/Settings';
import AppsIcon from '@mui/icons-material/Apps';

const Navbar = ({ setSearchTerm }) => {
  const [searchText, setSearchText] = useState('');
  const handleSearch = (e) => {
    e.preventDefault();
    setSearchTerm(searchText); // set the search term in App component state
  };

  return (
    <div className='header'>
      <div className="header__logo">
        <img src={DFSLogo} alt="DFS" />
        <span className='span'>FileArc              </span> {/*tabs added for searchbar alginment*/}
      </div>
      <div className="header__searchContainer">
        <div className="header__searchBar">
          <SearchIcon />
          <span></span>
          <input type="text" placeholder='Search in Distributed File System' onChange={(e) => setSearchTerm(e.target.value)} />
          <ExpandMoreIcon />
        </div>
      </div>
      <div className="header__icons">
        <span>
          <HelpOutlineIcon />
          <SettingsIcon />
        </span>
        <AppsIcon />
      </div>
    </div>
  )
}

export default Navbar