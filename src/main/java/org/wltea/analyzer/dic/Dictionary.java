/**
 * IK 中文分词  版本 5.0
 * IK Analyzer release 5.0
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 *
 *
 */
package org.wltea.analyzer.dic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.core.CollectionUtils;

/**
 * 词典管理类,单子模式
 */
public class Dictionary {

    /*
     * 词典单子实例
     */
    private static Dictionary singleton;

    private DictSegment _MainDict;

    private DictSegment _SurnameDict;

    private DictSegment _QuantifierDict;

    private DictSegment _SuffixDict;

    private DictSegment _PrepDict;

    private DictSegment _StopWords;

    /**
     * 配置对象
     */
    private Configuration configuration;
    private ESLogger logger = null;
    public static final String PATH_DIC_MAIN = "ik/main.dic";
    public static final String PATH_DIC_SURNAME = "ik/surname.dic";
    public static final String PATH_DIC_QUANTIFIER = "ik/quantifier.dic";
    public static final String PATH_DIC_SUFFIX = "ik/suffix.dic";
    public static final String PATH_DIC_PREP = "ik/preposition.dic";
    public static final String PATH_DIC_STOP = "ik/stopword.dic";

    private Dictionary() {
        logger = Loggers.getLogger("ik-analyzer");
    }

    /**
     * 词典初始化
     * 由于IK Analyzer的词典采用Dictionary类的静态方法进行词典初始化
     * 只有当Dictionary类被实际调用时，才会开始载入词典，
     * 这将延长首次分词操作的时间
     * 该方法提供了一个在应用加载阶段就初始化字典的手段
     * @return Dictionary
     */
    public static Dictionary initial(Configuration cfg) {
        if (singleton == null) {
            synchronized (Dictionary.class) {
                if (singleton == null) {
                    singleton = new Dictionary();
                    singleton.configuration = cfg;
                    singleton.loadMainDict();
                    singleton.loadSurnameDict();
                    singleton.loadQuantifierDict();
                    singleton.loadSuffixDict();
                    singleton.loadPrepDict();
                    singleton.loadStopWordDict();
                    return singleton;
                }
            }
        }
        return singleton;
    }

    /**
     * 获取词典单子实例
     * @return Dictionary 单例对象
     */
    public static Dictionary getSingleton() {
        if (singleton != null) return singleton;
        throw new IllegalStateException("词典尚未初始化，请先调用initial方法");
    }

    /**
     * 批量加载新词条
     * @param words Collection<String>词条列表
     */
    public List<String> addWords(Collection<String> words) {
        List<String> result = Lists.<String>newArrayList();
        if (words == null) return result;

        for (String word : words) {
            if (word == null) continue;

            //批量加载词条到主内存词典中
            if (singleton._MainDict.fillSegment(word.trim()
                    .toLowerCase().toCharArray())) result.add(word);
        }
        return result;
    }

    /**
     * 批量移除（屏蔽）词条
     */
    public List<String> disableWords(Collection<String> words) {
        List<String> result = Lists.<String>newArrayList();
        if (words == null) return result;

        for (String word : words) {
            if (word == null) continue;

            //批量屏蔽词条
            if (singleton._MainDict.disableSegment(word.trim()
                    .toLowerCase().toCharArray())) result.add(word);
        }
        return result;
    }

    /**
     * 检索匹配主词典
     * @return Hit 匹配结果描述
     */
    public Hit matchInMainDict(char[] charArray) {
        return singleton._MainDict.match(charArray);
    }

    /**
     * 检索匹配主词典
     * @return Hit 匹配结果描述
     */
    public Hit matchInMainDict(char[] charArray, int begin, int length) {
        return singleton._MainDict.match(String.valueOf(charArray).toLowerCase().toCharArray(), begin, length);
    }

    /**
     * 检索匹配量词词典
     * @return Hit 匹配结果描述
     */
    public Hit matchInQuantifierDict(char[] charArray, int begin, int length) {
        return singleton._QuantifierDict.match(String.valueOf(charArray).toLowerCase().toCharArray(), begin, length);
    }

    /**
     * 从已匹配的Hit中直接取出DictSegment，继续向下匹配
     * @return Hit
     */
    public Hit matchWithHit(char[] charArray, int currentIndex, Hit matchedHit) {
        DictSegment ds = matchedHit.getMatchedDictSegment();
        return ds.match(charArray, currentIndex, 1, matchedHit);
    }

    /**
     * 判断是否是停止词
     * @return boolean
     */
    public boolean isStopWord(char[] charArray, int begin, int length) {
        return singleton._StopWords.match(String.valueOf(charArray).toLowerCase().toCharArray(), begin, length).isMatch();
    }

    /**
     * 加载主词典及扩展词典
     */
    private void loadMainDict() {
        //建立一个主词典实例
        _MainDict = new DictSegment((char) 0);

        //读取主词典文件
        File file = new File(configuration.getDictRoot(), Dictionary.PATH_DIC_MAIN);

        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
            String theWord = null;
            do {
                theWord = br.readLine();
                if (theWord != null && !"".equals(theWord.trim())) {
                    _MainDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                }
            } while (theWord != null);

        } catch (IOException e) {
            logger.error("ik-analyzer", e);

        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                logger.error("ik-analyzer", e);
            }
        }
        //加载扩展词典
        this.loadExtDict();
        //加载ROC动态增减的词典
        this.loadRocDict();
    }

    /**
     * 加载用户配置的扩展词典到主词库表
     */
    private void loadExtDict() {
        //加载扩展词典配置
        List<String> extDictFiles = configuration.getExtDictionarys();
        if (extDictFiles == null) return;

        InputStream is = null;
        for (String extDictName : extDictFiles) {
            //读取扩展词典文件
            logger.info("[Dict Loading]" + extDictName);
            File file = new File(configuration.getDictRoot(), extDictName);
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                logger.error("ik-analyzer", e);
            }

            //如果找不到扩展的字典，则忽略
            if (is == null) continue;

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
                String theWord = null;
                do {
                    theWord = br.readLine();
                    if (theWord != null && !"".equals(theWord.trim())) {
                        //加载扩展词典数据到主内存词典中
                        _MainDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                    }
                } while (theWord != null);

            } catch (IOException e) {
                logger.error("ik-analyzer", e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                } catch (IOException e) {
                    logger.error("ik-analyzer", e);
                }
            }
        }
    }

    private static final String PATH_DIC_ROC_ADD = "ik/roc_add.dic";
    private static final String PATH_DIC_ROC_REMOVE = "ik/roc_remove.dic";

    /**
     * 加载ROC动态增减的词典
     */
    private void loadRocDict() {
        this.loadRocDict(PATH_DIC_ROC_ADD, true);
        this.loadRocDict(PATH_DIC_ROC_REMOVE, false);
    }

    private void loadRocDict(String path, boolean add) {
        logger.info("[Dict Loading]" + path);
        File file = new File(configuration.getDictRoot(), path);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error("ik-analyzer", e);
        }
        if (is == null) return;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
            String theWord = null;
            do {
                theWord = br.readLine();
                if (theWord != null && !"".equals(theWord.trim())) {
                    if (add) _MainDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                    else _MainDict.disableSegment(theWord.trim().toLowerCase().toCharArray());
                }
            } while (theWord != null);

        } catch (IOException e) {
            logger.error("ik-analyzer", e);
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                logger.error("ik-analyzer", e);
            }
        }
    }

    /**
     * 动态新增ROC词典
     */
    public void addRocDict(Collection<String> words) {
        Collection<String> add = this.removeRocDict(PATH_DIC_ROC_REMOVE, words);
        this.appendRocDict(PATH_DIC_ROC_ADD, add);
    }

    /**
     * 动态清除ROC词典
     */
    public void removeRocDict(Collection<String> words) {
        Collection<String> remove = this.removeRocDict(PATH_DIC_ROC_ADD, words);
        this.appendRocDict(PATH_DIC_ROC_REMOVE, remove);
    }

    /**
     * 追加ROC动态词典
     */
    private void appendRocDict(String path, Collection<String> words) {
        File file = new File(configuration.getDictRoot(), path);
        OutputStream os = null;
        try {
            os = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            logger.error("ik-analyzer", e);
        }
        if (os == null) return;

        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"), 512);
            for (String word : words) {
                bw.write(word);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            logger.error("ik-analyzer", e);
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException e) {
                logger.error("ik-analyzer", e);
            }
        }
    }

    /**
     * 清除ROC动态词典中的已有词, 返回未命中的待清除词
     */
    private Collection<String> removeRocDict(String path, Collection<String> words) {
        File file = new File(configuration.getDictRoot(), path);

        //读取当前词典
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error("ik-analyzer", e);
        }
        if (is == null) return words;

        List<String> original = new LinkedList<String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
            String theWord = null;
            do {
                theWord = br.readLine();
                if (theWord != null && !"".equals(theWord.trim())) {
                    original.add(theWord);
                }
            } while (theWord != null);

        } catch (IOException e) {
            logger.error("ik-analyzer", e);
            return words;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                logger.error("ik-analyzer", e);
            }
        }

        //求交集
        Collection inter = CollectionUtils.intersection(original, words);
        //重新写入清除后的词典
        OutputStream os = null;
        try {
            os = new FileOutputStream(file, false);
        } catch (FileNotFoundException e) {
            logger.error("ik-analyzer", e);
        }
        if (os == null) return words;

        //已有词典去除交集
        original.removeAll(inter);
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"), 512);
            for (String word : original) {
                bw.write(word);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            logger.error("ik-analyzer", e);
            return words;
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException e) {
                logger.error("ik-analyzer", e);
            }
        }
        //待清除词去除交集
        words.removeAll(inter);
        return words;
    }

    /**
     * 加载用户扩展的停止词词典
     */
    private void loadStopWordDict() {
        //建立主词典实例
        _StopWords = new DictSegment((char) 0);

        //读取主词典文件
        File file = new File(configuration.getDictRoot(), Dictionary.PATH_DIC_STOP);

        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
            String theWord = null;
            do {
                theWord = br.readLine();
                if (theWord != null && !"".equals(theWord.trim())) {
                    _StopWords.fillSegment(theWord.trim().toLowerCase().toCharArray());
                }
            } while (theWord != null);

        } catch (IOException e) {
            logger.error("ik-analyzer", e);

        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                logger.error("ik-analyzer", e);
            }
        }

        //加载扩展停止词典
        List<String> extStopWordDictFiles = configuration.getExtStopWordDictionarys();
        if (extStopWordDictFiles != null) {
            is = null;
            for (String extStopWordDictName : extStopWordDictFiles) {
                logger.info("[Dict Loading]" + extStopWordDictName);

                //读取扩展词典文件
                file = new File(configuration.getDictRoot(), extStopWordDictName);
                try {
                    is = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    logger.error("ik-analyzer", e);
                }
                //如果找不到扩展的字典，则忽略
                if (is == null) continue;

                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
                    String theWord = null;
                    do {
                        theWord = br.readLine();
                        if (theWord != null && !"".equals(theWord.trim())) {
                            //加载扩展停止词典数据到内存中
                            _StopWords.fillSegment(theWord.trim().toLowerCase().toCharArray());
                        }
                    } while (theWord != null);

                } catch (IOException e) {
                    logger.error("ik-analyzer", e);

                } finally {
                    try {
                        if (is != null) {
                            is.close();
                            is = null;
                        }
                    } catch (IOException e) {
                        logger.error("ik-analyzer", e);
                    }
                }
            }
        }
    }

    /**
     * 加载量词词典
     */
    private void loadQuantifierDict() {
        //建立一个量词典实例
        _QuantifierDict = new DictSegment((char) 0);
        //读取量词词典文件
        File file = new File(configuration.getDictRoot(), Dictionary.PATH_DIC_QUANTIFIER);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error("ik-analyzer", e);
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
            String theWord = null;
            do {
                theWord = br.readLine();
                if (theWord != null && !"".equals(theWord.trim())) {
                    _QuantifierDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                }
            } while (theWord != null);

        } catch (IOException ioe) {
            logger.error("Quantifier Dictionary loading exception.");

        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                logger.error("ik-analyzer", e);
            }
        }
    }

    private void loadSurnameDict() {
        _SurnameDict = new DictSegment((char) 0);
        File file = new File(configuration.getDictRoot(), Dictionary.PATH_DIC_SURNAME);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error("ik-analyzer", e);
        }
        if (is == null) throw new RuntimeException("Surname Dictionary not found!!!");

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
            String theWord;
            do {
                theWord = br.readLine();
                if (theWord != null && !"".equals(theWord.trim())) {
                    _SurnameDict.fillSegment(theWord.trim().toCharArray());
                }
            } while (theWord != null);
        } catch (IOException e) {
            logger.error("ik-analyzer", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                logger.error("ik-analyzer", e);
            }
        }
    }

    private void loadSuffixDict() {
        _SuffixDict = new DictSegment((char) 0);
        File file = new File(configuration.getDictRoot(), Dictionary.PATH_DIC_SUFFIX);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error("ik-analyzer", e);
        }
        if (is == null) throw new RuntimeException("Suffix Dictionary not found!!!");

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
            String theWord;
            do {
                theWord = br.readLine();
                if (theWord != null && !"".equals(theWord.trim())) {
                    _SuffixDict.fillSegment(theWord.trim().toCharArray());
                }
            } while (theWord != null);
        } catch (IOException e) {
            logger.error("ik-analyzer", e);
        } finally {
            try {
                is.close();
                is = null;
            } catch (IOException e) {
                logger.error("ik-analyzer", e);
            }
        }
    }

    private void loadPrepDict() {
        _PrepDict = new DictSegment((char) 0);
        File file = new File(configuration.getDictRoot(), Dictionary.PATH_DIC_PREP);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error("ik-analyzer", e);
        }
        if (is == null) throw new RuntimeException("Preposition Dictionary not found!!!");

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
            String theWord;
            do {
                theWord = br.readLine();
                if (theWord != null && !"".equals(theWord.trim())) {
                    _PrepDict.fillSegment(theWord.trim().toCharArray());
                }
            } while (theWord != null);
        } catch (IOException e) {
            logger.error("ik-analyzer", e);
        } finally {
            try {
                is.close();
                is = null;
            } catch (IOException e) {
                logger.error("ik-analyzer", e);
            }
        }
    }

}
