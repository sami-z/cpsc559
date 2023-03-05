import React, { useState, useEffect } from 'react'
import FileItem from './FileItem'
import './styles.css'
const Files = () => {
    //const [files, setFiles] = useState([])

    // HARDCODED files Array 
    let files = [
        {
            caption: "rag",
            timestamp: "1234",
            fileUrl: "rag.com",
            size: "12314"
        },
        {
            caption: "sam",
            timestamp: "234",
            fileUrl: "sam.com",
            size: "123"
        }
    ]
    // let files = [1, 2, 3, 4]

    return (
        <div className="fileView">
            <div className='fileView_row'>

            </div>
            <div className='fileView_titles'>
                <div className='fileView_title_left'>
                    <p>Name</p>
                </div>
                <div className='fileView_title_right'>
                    <p>Last Modified</p>
                    <p>File Size</p>
                </div>
            </div>
            {
                files.map((item, id) => (
                    <FileItem id={id} caption={item.caption} timestamp={item.timestamp} fileUrl={item.fileUrl} size={item.size} />
                ))
            }

        </div>

    )
}

export default Files