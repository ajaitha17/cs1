package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    File file;
    TupleDesc tupleDesc;
    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        file = f;
        tupleDesc = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        return file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        return tupleDesc;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // int offset=pid.pageNumber();
        // DbFile dbf = new HeapFile(this.file, this.tupleDesc);
        // return dbf.readPage(pid);
         try {
            RandomAccessFile f = new RandomAccessFile(file, "rw");
            int offset = BufferPool.PAGE_SIZE * pid.pageNumber();
            byte[] data = new byte[BufferPool.PAGE_SIZE];
            if (offset + BufferPool.PAGE_SIZE > f.length()) {
                return null;
            }
            f.seek(offset);
            f.read(data);
            return new HeapPage((HeapPageId) pid, data);
        } catch (Exception e) {
            return null;
        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        int numPage=(int) Math.ceil(file.length()/BufferPool.PAGE_SIZE);
        return numPage;
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        return (DbFileIterator) new HeapFileIterator(this, tid);
    }

    public class HeapFileIterator implements DbFileIterator {
        Iterator<Tuple> tuples;
        int currPage;
        TransactionId tid;
        HeapFile heapfile;
        Tuple next;

        public HeapFileIterator(HeapFile hf, TransactionId tID) {
            tid = tID;
            heapfile = hf;
            currPage = -999;
            next = null;
        }

        public void open() {
            currPage = 0;
        }

        public Tuple readNext() {
            if(tuples != null) {
                if(!tuples.hasNext())
                    tuples = null;
            } 

            try {
                while(tuples == null && currPage < heapfile.numPages()) {
                        HeapPageId id = new HeapPageId(heapfile.getId(), currPage);
                        BufferPool bf = Database.getBufferPool();
                        HeapPage page = (HeapPage) bf.getPage(tid,id,Permissions.READ_ONLY);
                        if(page.iterator().hasNext())
                            tuples = page.iterator();
                        currPage++;
                    }
                }
            catch (Exception e) {
                return null;
            }

            if (tuples == null)
                return null;

            return tuples.next();
        }

        public void rewind() {
            close();
            currPage = 0;
        }

        public Tuple next() {
            if(next==null)
                throw new NoSuchElementException();
            Tuple n = next;
            next = null;
            return n;
        }

        public boolean hasNext() {
            if(next==null)
                next = readNext();
            if(next==null)
                return false;
            return true;
        }

        public void close() {
            next=null;
            tuples = null;
            currPage = -999;
        }
    }

}

