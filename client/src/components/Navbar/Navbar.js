import React from 'react'
import './styles.css'
import DFSLogo from '../../media/logo-black.png'

import SearchIcon from '@mui/icons-material/Search';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import SettingsIcon from '@mui/icons-material/Settings';
//import AccountCircleIcon from '@material-ui/icons/AccountCircle';
import AppsIcon from '@mui/icons-material/Apps';

const Navbar = () => {

  return (
    <div className='header'>
      <div className="header__logo">
        <img src={DFSLogo} alt="DFS" />
        <span>Distributed File System</span>
      </div>
      <div className="header__searchContainer">
        <div className="header__searchBar">
          <SearchIcon />
          <input type="text" placeholder='Search in Distributed File System' />
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