import React from 'react'
import Upload from './Upload'
import Share from './Share'
import Delete from './Delete'
import './styles.css'
const Sidebar = () => {
  return (
    <div className='sidebar'>
      <Upload />
      <Share />
      <Delete />
    </div>
  )
}

export default Sidebar