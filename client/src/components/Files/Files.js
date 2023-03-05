import React, { useState, useEffect } from 'react'
import FileItem from './FileItem'
import FileCard from './FileCard'
import './styles.css'

const Files = ({ searchTerm }) => {
    // const [files, setFiles] = useState([])

    // HARDCODED files Array 
    let files = [
        {
            id: 1,
            caption: "ragSmart.jpg",
            timestamp: "1234",
            fileUrl: "rag.com",
            size: "12314"
        },
        {
            id: 2,
            caption: "ragDaddy.txt",
            timestamp: "234",
            fileUrl: "sam.com",
            size: "123"
        },
        {
            id: 3,
            caption: "jake.jpg",
            timestamp: "234",
            fileUrl: "sam.com",
            size: "123"
        },
        {
            id: 4,
            caption: "awwstin.txt",
            timestamp: "234",
            fileUrl: "sam.com",
            size: "123"
        },
        {
            id: 5,
            caption: "mana.csv",
            timestamp: "234",
            fileUrl: "sam.com",
            size: "123"
        },
        {
            id: 6,
            caption: "hello-world.txt",
            timestamp: "234",
            fileUrl: "sam.com",
            size: "123"
        },
        {
            id: 7,
            caption: "cpsc559.iml",
            timestamp: "234",
            fileUrl: "sam.com",
            size: "123"
        },
        
    ]

    // Filter the files array based on whether the caption property includes the searchTerm
    const filteredFiles = files.filter((item) => item.caption.toLowerCase().includes(searchTerm.toLowerCase()));

    return (
        <div className="fileView">
            <h5 style={{ fontSize: 20, marginBottom: '-10px', fontWeight: 'normal' }}>Recent Files</h5>

            <div className='fileView_row'>
                {
                    files.slice(0, 6).map(({ id, caption }) => (
                        <FileCard key={id} name={caption} className="fileCard" />
                    ))
                }

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
                filteredFiles.map((item, id) => (
                    <FileItem key={id} caption={item.caption} timestamp={item.timestamp} fileUrl={item.fileUrl} size={item.size} />
                ))
            }

        </div>

    )
}

export default Files