package be.wegenenverkeer.atomium.server.jdbc

trait PostgresDialect extends Dialect {

  override def createFeedTableStatement: String = {
    s"""CREATE TABLE IF NOT EXISTS ${FeedDbModel.Table.name} (
         |${FeedDbModel.Table.idColumn} SERIAL primary key,
         |${FeedDbModel.Table.nameColumn} varchar NOT NULL,
         |${FeedDbModel.Table.titleColumn} varchar,
         |UNIQUE(${FeedDbModel.Table.nameColumn}));""".stripMargin
  }


  override def createEntryTableStatement(entryTableName: String): String = {
    s"""CREATE TABLE IF NOT EXISTS $entryTableName (
         |${EntryDbModel.Table.idColumn} SERIAL primary key,
         |${EntryDbModel.Table.uuidColumn} varchar,
         |${EntryDbModel.Table.valueColumn} text,
         |${EntryDbModel.Table.timestampColumn} timestamp not null);""".stripMargin
  }

  override def dropFeedTable(implicit jdbcContext: JdbcContext): Unit = {
    sqlUpdate(s"DROP TABLE ${FeedDbModel.Table.name};")
  }

  override def fetchFeed(feedName: String)(implicit jdbcContext: JdbcContext): Option[FeedDbModel] = {

    val feeds = sqlQuery(
      s"""SELECT * FROM ${FeedDbModel.Table.name}
         | WHERE ${FeedDbModel.Table.nameColumn} = '$feedName';
       """.stripMargin, None, FeedDbModel.apply)

    feeds.headOption
  }

  override def addFeed(feed: FeedDbModel)(implicit jdbcContext: JdbcContext): Unit = {

    val titleData = feed.title.orNull

    sqlUpdatePepared(
      s"""INSERT INTO ${FeedDbModel.Table.name} (${FeedDbModel.Table.nameColumn}, ${FeedDbModel.Table.titleColumn})
         |VALUES (?, ?);
       """.stripMargin, feed.name, titleData)
  }


  override def dropEntryTable(entryTableName: String)(implicit jdbcContext: JdbcContext): Unit = {
    sqlUpdate(s"DROP TABLE $entryTableName")
  }

  override def fetchFeedEntries(entryTableName: String, start: Long, count: Int, ascending: Boolean)(implicit jdbcContext: JdbcContext): List[EntryDbModel] = {

    val (comparator, direction) = if (ascending) (">=", "ASC") else ("<=", "DESC")

    sqlQuery(
      s"""SELECT * FROM $entryTableName
         |WHERE ${EntryDbModel.Table.idColumn} $comparator $start ORDER BY ${EntryDbModel.Table.idColumn} $direction;
       """.stripMargin,
      Some(count),
      EntryDbModel.apply
    )
  }

  override def fetchMostRecentFeedEntries(entryTableName: String, count: Int)(implicit jdbcContext: JdbcContext): List[EntryDbModel] = {
    sqlQuery(
      s"""SELECT * FROM $entryTableName
         |ORDER BY ${EntryDbModel.Table.idColumn} DESC;
       """.stripMargin,
      Some(count),
      EntryDbModel.apply
    )
  }

  override def addFeedEntry(entryTableName: String, entryData: EntryDbModel)(implicit jdbcContext: JdbcContext): Unit = {

    val preparedSql =
      s"""INSERT INTO $entryTableName (${EntryDbModel.Table.uuidColumn}, ${EntryDbModel.Table.valueColumn}, ${EntryDbModel.Table.timestampColumn})
         |VALUES (?,?,?);
       """.stripMargin

    sqlUpdatePepared(preparedSql, entryData.uuid, entryData.value, entryData.timestamp)
  }

  /**
   * Fetch the largest entry ID from the database.
   *
   * @param entryTableName The name of the entry table.
   * @param jdbcContext The JDBC context to use.
   * @return The largest entry id for a given entry table, or -1 if the entry table is empty.
   */
  override def fetchMaxEntryId(entryTableName: String)(implicit jdbcContext: JdbcContext): Long = {

    val maxList: List[Long] = sqlQuery[Long](
      s"SELECT max(${EntryDbModel.Table.idColumn}) as max FROM $entryTableName;",
      None,
      _.getLong("max")
    )

    maxList.headOption.getOrElse(-1)
  }

  override def fetchEntryCountLowerThan(entryTableName: String, sequenceNo: Long, inclusive: Boolean)(implicit jdbcContext: JdbcContext): Long = {

    val comparator = if (inclusive) "<=" else "<"

    val countList: List[Long] =
      sqlQuery[Long](
        s"""SELECT count(*) as total FROM $entryTableName
         |WHERE ${EntryDbModel.Table.idColumn} $comparator $sequenceNo;
       """.stripMargin,
        None,
        _.getLong("total")
      )

    countList.headOption.getOrElse(0)
  }

}