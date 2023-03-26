import React from 'react'
import Upload from './Upload'
import Share from './Share'
import Delete from './Delete'
import './styles.css'
const Sidebar = ({selectedFiles}) => {
  return (
    <div className='sidebar'>
      <Upload />
      <Share selectedFiles={selectedFiles}/>
      <Delete selectedFiles={selectedFiles} />
    </div>
  )
}

export default Sidebar