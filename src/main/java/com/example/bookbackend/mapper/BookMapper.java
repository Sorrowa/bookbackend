package com.example.bookbackend.mapper;

import com.example.bookbackend.bean.Book;
import com.example.bookbackend.provider.BookSqlProvider;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

@Mapper
public interface BookMapper {

    @InsertProvider(type = BookSqlProvider.class,method = "insert")
    @Options(useGeneratedKeys = true, keyColumn = "book_id", keyProperty = "bookId")
    Long insert(Book book);

    @Select("select * from book where book_id=#{bookId}")
    Book selectById(Integer bookId);//commentmapper关联查询用
    @Select("select * from book where email=#{email} and state=1 order by book_id desc")
    List<Book> selectByEmail(@Param("email") String email);
    @Select("select * from book where find_in_set(#{type},type) and state=1 order by book_id desc")
    List<Book> selectByType(@Param("type") String type);
    @Select("select * from book where state=1 and name like #{name} order by book_id desc")
    List<Book> selectByName(@Param("name") String name);
    @Select("select * from book where state=1 order by book_id desc limit 50")
    List<Book> selectAll();

    @Select("select * from book where (state=2 or state=3 or state=4) and email=#{email}")
    @Results({
            @Result(column="book_id", property="bookId",id=true),
            @Result(column="change_id", property="changeId"),
            @Result(property = "changeBook", column = "change_id",
                    one = @One(select = "com.example.bookbackend.mapper.BookMapper.selectById"))
    })
    List<Book> selectChanges(@Param("email") String email);

    @Select("select * from book where book_id = #{bookId}")
    @Results({
            @Result(column="book_id", property="bookId",id=true),
            @Result(property = "comments", column = "book_id",
                    many = @Many(select = "com.example.bookbackend.mapper.CommentMapper.selectByBookId"))
    })
    Book selectWithComments(@Param("bookId") Integer bookId);

    @Update("update book set img_path=#{imgPath} where book_id=#{bookId}")
    Long updateImage(@Param("bookId") Integer bookId,@Param("imgPath") String imgPath);

    @Update("update book set state=#{state},changer=#{changer},change_id=#{changeId} where book_id=#{bookId}")
    Long changeBook(Integer bookId,String changer,Integer changeId,Integer state);

    @Update("update book set state=1 where book_id=#{bookId}")
    Long refuseChange(Integer bookId);

    @Update("update book set state=4 where book_id=#{bookId}")
    Long confirmChange(Integer bookId);




    @Select("select email from book where book_id=#{bookId}")
    String selectOwner(Integer bookId);

    @Select("select change_id from book where book_id=#{bookId}")
    Integer selectChangeID(Integer bookId);

    @Delete("delete from book where book_id=#{bookId}")
    Integer deleteById(Integer bookId);
}
