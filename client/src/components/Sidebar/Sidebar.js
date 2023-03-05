import React from 'react'
import Upload from './Upload'
import Share from './Share'
import './styles.css'
const Sidebar = () => {
  return (
    <div className='sidebar'>
      <Upload />
      <Share />
    </div>
  )
}

export default Sidebar