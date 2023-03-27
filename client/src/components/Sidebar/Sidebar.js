import React from 'react'
import Upload from './Upload'
import Share from './Share'
import Delete from './Delete'
import './styles.css'
const Sidebar = ({selectedFiles, userName}) => {
  return (
    <div className='sidebar'>
      <Upload userName={userName}/>
      <Share selectedFiles={selectedFiles} userName={userName}/>
      <Delete selectedFiles={selectedFiles} userName={userName}/>
    </div>
  )
}

export default Sidebar